package gov.nist.itl.ssd.wipp.backend.argo.workflows.workflow;

import gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin.Plugin;
import gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin.PluginRepository;
import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.Workflow;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowRepository;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import io.swagger.annotations.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author Philippe Dessauw <philippe.dessauw at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Controller
@Api(tags="Workflow Entity")
@RequestMapping(CoreConfig.BASE_URI + "/workflows/{workflowId}/submit")
public class WorkflowSubmitController {
    @Autowired
    CoreConfig config;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PluginRepository wippPluginRepository;

    @Autowired
    private WorkflowConverter converter;

    @RequestMapping(
        value = "",
        method = RequestMethod.POST,
        produces = { "application/json" }
    )
    public ResponseEntity<Workflow> submit(
        @PathVariable("workflowId") String workflowId
    ) {
        // Retrieve Workflow object
        Optional<Workflow> wippWorkflow = workflowRepository.findById(
            workflowId
        );

        if(!wippWorkflow.isPresent()) {
        	throw new ClientException("Received submission of unknown workflow");
        }

        Workflow workflow = wippWorkflow.get();

        // Build the list of jobs, dependencies and plugins
        List<Job> jobList = jobRepository.findByWippWorkflow(workflowId);

        Map<Job, List<String>> jobsDependencies = new HashMap<>();
        Map<Job, Plugin> jobsPlugins = new HashMap<>();

        for(Job job: jobList) {
            // Link dependencies to job
            List<String> dependencies = new ArrayList<>();

            if(job.getDependencies() != null) {
                for(String dependencyId: job.getDependencies()) {
                    Optional<Job> optionalJobDependency = jobRepository.findById(dependencyId);

                    if(!optionalJobDependency.isPresent()) {
        				throw new ClientException("Error while submitting workflow: unknown job dependency " + dependencyId);
                    }

                    Job jobDependency = optionalJobDependency.get();
                    dependencies.add(jobDependency.getName());
                }
            }

            jobsDependencies.put(job, dependencies);

            // Link plugin to the job
            Optional<Plugin> plugin = wippPluginRepository.findById(job.getWippExecutable());
            if(!plugin.isPresent()) {
				throw new ClientException("Error while submitting workflow: unknown plugin " + job.getWippExecutable());
            }

            jobsPlugins.put(job, plugin.get());
        }

        // Start the conversion
        try {
            File workflowFolder = new File(config.getWorkflowsFolder(), workflowId);
            if(!workflowFolder.exists()) {
                if(!workflowFolder.mkdirs()) {
                    throw new IOException("Problem when creating the workflow folder");
                }
            }

            converter.convert(workflow, jobsDependencies, jobsPlugins, workflowFolder + File.separator + "workflow-" + workflowId + ".yaml");

            // Save the workflow and send the HTTP response
            Workflow submittedWorkflow = converter.getWorkflow();
            workflowRepository.save(submittedWorkflow);
            return new ResponseEntity<>(submittedWorkflow, HttpStatus.OK);
        } catch (Exception exc) {
        	throw new ClientException("Error while submitting workflow " + exc.getMessage());
        }

    }
}
