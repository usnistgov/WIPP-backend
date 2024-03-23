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
package gov.nist.itl.ssd.wipp.backend.argo.workflows.workflow;

import gov.nist.itl.ssd.wipp.backend.core.model.computation.Plugin;
import gov.nist.itl.ssd.wipp.backend.core.model.computation.PluginIO;
import gov.nist.itl.ssd.wipp.backend.core.model.computation.PluginRepository;
import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandlerService;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobStatus;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.Workflow;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowStatus;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Controller
@Tag(name="Workflow Entity")
@RequestMapping(CoreConfig.BASE_URI + "/workflows/{workflowId}/exit")
public class WorkflowExitController {

    @Autowired
    CoreConfig config;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private PluginRepository wippPluginRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private DataHandlerService dataHandlerService;

    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            produces = {"application/json"}
    )
    public ResponseEntity<Workflow> exit(
            @PathVariable("workflowId") String workflowId,
            @RequestBody String status
    ) {
    	
		// Load security context for system operations
    	SecurityUtils.runAsSystem();

        // Retrieve Workflow object
        Optional<Workflow> wippWorkflow = workflowRepository.findById(
                workflowId
        );

        if (!wippWorkflow.isPresent()) {
            throw new ClientException("Received status for unknown workflow");
        }

        Workflow workflow = wippWorkflow.get();
        
        // Only statuses from running or submitted workflows can be updated
        if (workflow.getStatus() != WorkflowStatus.SUBMITTED && 
        		workflow.getStatus() != WorkflowStatus.RUNNING) {
            throw new ClientException("Workflow status cannot be updated if current status is not SUBMITTED or RUNNING");
        }

        // Check validity of status
        WorkflowStatus wfStatus;
        status = status.toUpperCase();
        try {
            wfStatus = WorkflowStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new ClientException("Received unknown status " + status + " for workflow " + workflowId);
        }

        boolean success = false;
        String errorMessage = "";

        switch (wfStatus) {
            case SUCCEEDED:
                success = true;
                break;
            case ERROR:
            case FAILED:
                errorMessage = "Error during workflow execution.";
                break;
            case CANCELLED:
                errorMessage = "Workflow cancelled.";
                break;
            default:
                throw new ClientException("Received non-exit status for workflow " + workflowId);
        }

        // Retrieve workflow's jobs to import results in case of success
        List<Job> jobList = jobRepository.findByWippWorkflow(workflowId);
        for (Job job : jobList) {
            job.setStatus(JobStatus.valueOf(status));
            if (success) {
                try {
                    Optional<Plugin> pluginOpt = wippPluginRepository.findById(job.getWippExecutable());
                    Plugin plugin = pluginOpt.get();
                    List<PluginIO> outputs = plugin.getOutputs();
                    for (PluginIO output : outputs) {
                        DataHandler dataHandler = dataHandlerService.getDataHandler(output.getType());
                        dataHandler.importData(job, output.getName());
                    }
                } catch (Exception e) {
                    job.setStatus(JobStatus.ERROR);
                    job.setError("Unable to import job result");
                }
            } else {
                job.setError(errorMessage);
            }
            jobRepository.save(job);
        }
        workflow.setEndTime(new Date());
        workflow.setStatus(wfStatus);
        workflowRepository.save(workflow);
        
        // Clear security context after system operations
        SecurityContextHolder.clearContext();

        return new ResponseEntity<>(workflow, HttpStatus.OK);
    }
}
