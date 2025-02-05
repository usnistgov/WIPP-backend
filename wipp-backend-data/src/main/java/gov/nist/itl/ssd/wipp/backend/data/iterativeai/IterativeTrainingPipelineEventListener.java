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

import gov.nist.itl.ssd.wipp.backend.core.model.events.AllImagesDoneConvertingEvent;
import gov.nist.itl.ssd.wipp.backend.core.model.events.WorkflowExecutionEndedEvent;
import gov.nist.itl.ssd.wipp.backend.core.model.events.WorkflowSubmissionFailedEvent;
import gov.nist.itl.ssd.wipp.backend.core.model.events.WorkflowSubmittedEvent;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.Workflow;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Event listener for Iterative AI trining pipelines
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Component
public class IterativeTrainingPipelineEventListener {

    @Autowired
    private IterativeTrainingPipelineRepository iterativeTrainingPipelineRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    private static final Logger LOGGER = Logger.getLogger(IterativeTrainingPipelineEventListener.class.getName());

    /**
     * Listen for WorkflowSubmittedEvent and update training iteration if needed
     */
    @EventListener
    void handleWorkflowSubmittedEvent(WorkflowSubmittedEvent event) {
        this.updateIterationStatusIfWorkflowOfInterest(event.getWorkflow(), IterativeTrainingPipeline.IterationStatus.RUNNING);
    }

    /**
     * Listen for WorkflowSubmissionFailedEvent and update training iteration if needed
     */
    @EventListener
    void handleWorkflowSubmissionFailedEvent(WorkflowSubmissionFailedEvent event) {
        this.updateIterationStatusIfWorkflowOfInterest(event.getWorkflow(), IterativeTrainingPipeline.IterationStatus.FAILED);
    }

    /**
     * Listen for WorkflowExecutionEndedEvent and update training iteration if needed
     */
    @EventListener
    void handleWorkflowExecutionEndedEvent(WorkflowExecutionEndedEvent event) {
        Workflow workflow = event.getWorkflow();
        IterativeTrainingPipeline.IterationStatus status = IterativeTrainingPipeline.IterationStatus.SUCCESSFUL;
        // any exit status other than "SUCCEEDED" is considered "FAILED" for the iteration
        if(!workflow.getStatus().equals(WorkflowStatus.SUCCEEDED)) status = IterativeTrainingPipeline.IterationStatus.FAILED;
        this.updateIterationStatusIfWorkflowOfInterest(event.getWorkflow(), status);
    }

    /**
     * Listen for AllImagesDoneConvertingEvent and update training iteration workflow status if needed
     */
    @EventListener
    void handleAllImagesDoneConvertingEvent(AllImagesDoneConvertingEvent event) {
        String collectionId = event.getCollectionId();
        if(collectionId != null) {
            IterativeTrainingPipeline pipeline = iterativeTrainingPipelineRepository.findOneByGroundTruthCollection(collectionId);
            if(pipeline != null && pipeline.getIterations() != null) {
                IterativeTrainingPipeline.TrainingIteration previousIteration = pipeline.getIterations().stream()
                        .filter(it -> (pipeline.getIterations().size()) == it.getIterationNumber())
                        .findFirst()
                        .orElse(null);
                if (previousIteration != null) {
                    Workflow iterationWorkflow = workflowRepository.findById(previousIteration.getTrainingWorkflow()).orElse(null);
                    if(iterationWorkflow != null && WorkflowStatus.PENDING.equals(iterationWorkflow.getStatus())) {
                        // change status from PENDING to CREATED so that it can be modified/submitted
                        iterationWorkflow.setStatus(WorkflowStatus.CREATED);
                        workflowRepository.save(iterationWorkflow);
                    }
                }
            }
        }
    }

    private void updateIterationStatusIfWorkflowOfInterest(Workflow workflow, IterativeTrainingPipeline.IterationStatus status) {
        // Check if workflow is part of existing pipeline
        IterativeTrainingPipeline pipeline = iterativeTrainingPipelineRepository.findOneByIterations_TrainingWorkflow(workflow.getId());
        // Update correspondent pipeline iteration status
        if(pipeline != null && pipeline.getIterations() != null) {
            pipeline.getIterations().stream()
                    .filter(it -> (workflow.getId()).equals(it.getTrainingWorkflow()))
                    .findFirst().orElseThrow(IllegalArgumentException::new)
                    .setStatus(status);
            iterativeTrainingPipelineRepository.save(pipeline);
            LOGGER.log(
                    Level.INFO,
                    "Updated iteration status of pipeline " + pipeline.getName()
                            + " to " + status.name() + " from workflow " + workflow.getName());
        }
    }
}
