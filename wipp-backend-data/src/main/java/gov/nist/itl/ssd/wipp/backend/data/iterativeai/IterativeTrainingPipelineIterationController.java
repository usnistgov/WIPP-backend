/*
 * This software was developed at the National Institute of Standards and
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
 */
package gov.nist.itl.ssd.wipp.backend.data.iterativeai;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.computation.Plugin;
import gov.nist.itl.ssd.wipp.backend.core.model.computation.PluginRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobStatus;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.Workflow;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowCopyService;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowStatus;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.data.imageannotations.ImageAnnotationsCollection;
import gov.nist.itl.ssd.wipp.backend.data.imageannotations.ImageAnnotationsCollectionRepository;
import gov.nist.itl.ssd.wipp.backend.data.imageannotations.annotations.ImageAnnotation;
import gov.nist.itl.ssd.wipp.backend.data.imageannotations.annotations.ImageAnnotationRepository;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.Image;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for AI training pieline iterations
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@RestController
@Tag(name="IterativeTrainingPipelines Entity")
@RequestMapping(CoreConfig.BASE_URI + "/iterativeTrainingPipelines/{iterativeTrainingPipelineId}/iterations")
public class IterativeTrainingPipelineIterationController {

    @Autowired
    CoreConfig config;

    @Autowired
    IterativeTrainingPipelineRepository iterativeTrainingPipelineRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    ImageAnnotationsCollectionRepository imageAnnotationsCollectionRepository;

    @Autowired
    private ImageAnnotationRepository imageAnnotationRepository;

    @Autowired
    WorkflowRepository workflowRepository;

    @Autowired
    PluginRepository pluginRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private WorkflowCopyService workflowCopyService;

    @PreAuthorize("isAuthenticated() and @iterativeTrainingPipelineSecurity.checkAuthorize(#iterativeTrainingPipelineId, true)")
    @RequestMapping(
            value = "add",
            method = RequestMethod.POST,
            produces = { "application/hal+json" }
    )
    /*
     * Add new training iteration
     */
    public EntityModel<IterativeTrainingPipeline> add(
            @PathVariable("iterativeTrainingPipelineId") String iterativeTrainingPipelineId
    ) {

        // TODO: catch exceptions and delete created objects

        // Retrieve IterativeTrainingPipeline object
        Optional<IterativeTrainingPipeline> iterativeTrainingPipeline = iterativeTrainingPipelineRepository.findById(
                iterativeTrainingPipelineId
        );

        // Sanity check - pipeline existence
        if (!iterativeTrainingPipeline.isPresent()) {
            throw new ClientException("Received submission of unknown pipeline");
        }

        // Get existing pipeline and retrieve iterations info
        IterativeTrainingPipeline pipeline = iterativeTrainingPipeline.get();
        List<IterativeTrainingPipeline.TrainingIteration> pipelineIterations = pipeline.getIterations();
        int newIterationNumber = pipeline.getCurrentIteration() + 1;
        if (pipelineIterations == null) {
            pipelineIterations = new ArrayList<>();
        }

        // Sanity check - previous iteration completed (success or failure/error)
        IterativeTrainingPipeline.TrainingIteration previousIteration = pipelineIterations.stream()
                .filter(it -> (newIterationNumber - 1) == it.getIterationNumber())
                .findAny()
                .orElse(null);
        if(previousIteration != null && previousIteration.getStatus() != null
                && previousIteration.getStatus() == IterativeTrainingPipeline.IterationStatus.RUNNING) {
            throw new ClientException("Cannot create new iteration while previous one is still running.");
        }

        // Create new iteration
        IterativeTrainingPipeline.TrainingIteration iteration = new IterativeTrainingPipeline.TrainingIteration();
        iteration.setIterationNumber(newIterationNumber);
        iteration.setStatus(IterativeTrainingPipeline.IterationStatus.ANNOTATION);
        String iterationName = pipeline.getName() + "-it" + newIterationNumber;

        // Select new images to annotate
        // If first iteration and startGroundTruthCollection is not empty, use starting masks in first iteration
        List<String> availableImages = imageRepository.findByImagesCollection(pipeline.getTrainingCollection())
                .stream()
                .filter(image -> !pipeline.getAnnotatedImages().contains(image.getFileName()))
                .map(Image :: getFileName)
                .collect(Collectors.toList());
        int numberOfImages = Math.min(pipeline.getImagesPerIteration(), availableImages.size());
        List<String> selectedImages = availableImages.subList(0, numberOfImages);

        // Setup annotation task
        ImageAnnotationsCollection annotationsCollection = new ImageAnnotationsCollection(iterationName);
        annotationsCollection.setImagesCollectionId(pipeline.getTrainingCollection());
        annotationsCollection.setTargetMaskCollectionId(pipeline.getGroundTruthCollection());
        annotationsCollection.setOwner(pipeline.getOwner());
        if(newIterationNumber > 1) {
            // get previous iteration's inference results
            // TODO: walk back iterations in case latest failed
            String maskCollection = null;
            if(previousIteration != null && previousIteration.getTrainingWorkflow() != null) {
                Workflow previousWf = workflowRepository.findById(previousIteration.getTrainingWorkflow()).orElse(null);
                if(previousWf != null && WorkflowStatus.SUCCEEDED.equals(previousWf.getStatus())) {
                    Job previousInferJob = jobRepository.findByWippWorkflow(previousWf.getId()).stream()
                            .filter(job -> (previousWf.getName() + "-infer").equals(job.getName()))
                            .findFirst()
                            .orElse(null);
                    if (previousInferJob != null) {
                        maskCollection = previousInferJob.getOutputParameter("outputDir");
                    }
                }
            }
            // set annotation starting point to previous iteration's inference results if any
            annotationsCollection.setStartMaskCollectionId(maskCollection);
        } else if(pipeline.getStartGroundTruthCollection() != null) {
            // set annotation starting point to start ground truth collection if any
            annotationsCollection.setStartMaskCollectionId(pipeline.getStartGroundTruthCollection());
            // set selectedImages to images in startGroundTruthCollection
            selectedImages = imageRepository.findByImagesCollection(pipeline.getStartGroundTruthCollection())
                    .stream()
                    .filter(image -> availableImages.contains(image.getFileName())) // sanity check - mask has matching image
                    .map(Image :: getFileName)
                    .collect(Collectors.toList());
        }
        annotationsCollection = imageAnnotationsCollectionRepository.save(annotationsCollection);
        ImageAnnotationsCollection finalAnnotationsCollection = annotationsCollection;
        selectedImages.forEach(image -> {
            ImageAnnotation annot = new ImageAnnotation();
            annot.setImageAnnotationsCollection(finalAnnotationsCollection.getId());
            annot.setPending(true);
            annot.setImageFileName(image);
            imageAnnotationRepository.save(annot);
            pipeline.getAnnotatedImages().add(image);
        });
        iteration.setImageAnnotationsCollection(annotationsCollection.getId());

        // Setup training-inference workflow
        Workflow workflow;
        if(newIterationNumber == 1 || previousIteration == null) {
            workflow = new Workflow();
            workflow.setName(iterationName);
            workflow.setCreationDate(new Date());
            workflow.setOwner(pipeline.getOwner());
            workflow.setStatus(WorkflowStatus.PENDING);
            workflow = workflowRepository.save(workflow);

            // Create training job
            Job trainingJob = new Job();
            trainingJob.setName(workflow.getName() + "-training");
            trainingJob.setWippWorkflow(workflow.getId());
            trainingJob.setWippVersion(config.getWippVersion());
            trainingJob.setStatus(JobStatus.CREATED);
            trainingJob.setCreationDate(new Date());
            trainingJob.setOwner(pipeline.getOwner());
            Plugin trainingPlugin = pluginRepository.findOneByNameAndVersion("WIPP UNet CNN Training Plugin", "1.0.0");
            trainingJob.setWippExecutable(trainingPlugin.getId());
            // add params
            Map<String, String> inputParameters = new HashMap<>();
            inputParameters.put("imageDir", pipeline.getTrainingCollection());
            inputParameters.put("maskDir", pipeline.getGroundTruthCollection());
            inputParameters.put("useTiling", "NO");
            inputParameters.put("trainFraction", "0.8");
            inputParameters.put("batchSize", "1");
            inputParameters.put("numberClasses", "2");
            inputParameters.put("learningRate", "3e-4");
            inputParameters.put("testEveryNSteps", "200");
            inputParameters.put("balanceClasses", "YES");
            inputParameters.put("earlyStoppingEpochCount", "5");
            inputParameters.put("useIntensityScaling", "YES");
            inputParameters.put("useAugmentation", "YES");
            inputParameters.put("augmentationReflection", "YES");
            inputParameters.put("augmentationRotation", "YES");
            trainingJob.setParameters(inputParameters);

            Map<String, String> outputParameters = new HashMap<>();
            outputParameters.put("outputDir", null);
            outputParameters.put("tensorboardDir", null);
            trainingJob.setOutputParameters(outputParameters);
            jobRepository.save(trainingJob);

            // Create inference job
            Job inferJob = new Job();
            inferJob.setName(workflow.getName() + "-infer");
            inferJob.setWippWorkflow(workflow.getId());
            inferJob.setWippVersion(config.getWippVersion());
            inferJob.setStatus(JobStatus.CREATED);
            inferJob.setCreationDate(new Date());
            inferJob.setOwner(pipeline.getOwner());
            Plugin inferPlugin = pluginRepository.findOneByNameAndVersion("WIPP UNet CNN Inference Plugin", "1.0.0");
            inferJob.setWippExecutable(inferPlugin.getId());
            List<String> dependencies = new ArrayList<>();
            dependencies.add(trainingJob.getId());
            inferJob.setDependencies(dependencies);
            // add params
            Map<String, String> inferInputParameters = new HashMap<>();
            inferInputParameters.put("imageDir", pipeline.getTrainingCollection());
            inferInputParameters.put("model", "{{ " + trainingJob.getId() + ".outputDir }}");
            inferInputParameters.put("useIntensityScaling", "YES");
            inferJob.setParameters(inferInputParameters);

            Map<String, String> inferOutputParameters = new HashMap<>();
            inferOutputParameters.put("outputDir", null);
            inferJob.setOutputParameters(inferOutputParameters);
            jobRepository.save(inferJob);
        } else {
            // Copy workflow from previous iteration if not first iteration
            workflow = workflowCopyService.copy(previousIteration.getTrainingWorkflow(), iterationName,
                    pipeline.getOwner(), WorkflowStatus.PENDING);
        }
        // Set Training workflow of iteration to newly created workflow
        iteration.setTrainingWorkflow(workflow.getId());

        // Add new iteration to pipeline's iterations list and update current iteration number
        pipelineIterations.add(iteration);
        pipeline.setIterations(pipelineIterations);
        pipeline.setCurrentIteration(newIterationNumber);

        // Save the pipeline and send the HTTP response
        iterativeTrainingPipelineRepository.save(pipeline);
        return EntityModel.of(pipeline);
    }

}
