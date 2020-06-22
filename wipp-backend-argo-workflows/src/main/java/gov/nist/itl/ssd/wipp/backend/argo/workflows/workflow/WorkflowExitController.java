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

import gov.nist.itl.ssd.wipp.backend.argo.workflows.persistence.ArgoWorkflow;
import gov.nist.itl.ssd.wipp.backend.argo.workflows.persistence.ArgoWorkflowRepository;
import gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin.Plugin;
import gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin.PluginIO;
import gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin.PluginRepository;
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
import io.swagger.annotations.Api;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.util.*;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Controller
@Api(tags="Workflow Entity")
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

    @Autowired
    private ArgoWorkflowRepository argoWorkflowRepository;


    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            produces = {"application/json"}
    )
    public ResponseEntity<Workflow> exit(
            @PathVariable("workflowId") String workflowId,
            @RequestBody String status
    ) {
        // Retrieve Workflow object
        Optional<Workflow> wippWorkflow = workflowRepository.findById(
                workflowId
        );

        if (!wippWorkflow.isPresent()) {
            throw new ClientException("Received status for unknown workflow");
        }

        Workflow workflow = wippWorkflow.get();
        ArgoWorkflow argoWorkflow = argoWorkflowRepository.findByName(workflow.getGeneratedName());

        // Check validity of status
        WorkflowStatus wfStatus;
        Map<String,String> jobsStatus = new HashMap<>();

        try {
            wfStatus = WorkflowStatus.valueOf(status.toUpperCase());

            try {
                JSONObject jsonWorkflow = new JSONObject(argoWorkflow.getWorkflow());
                JSONObject jsonStatus = jsonWorkflow.getJSONObject("status");
                JSONObject jsonNodes = jsonStatus.getJSONObject("nodes");
                Iterator<String> keys = jsonNodes.keys();
                while (keys.hasNext()){
                    String key = keys.next();
                    JSONObject job = jsonNodes.getJSONObject(key);

                    // check that the job is neither the DAG nor the exit handler
                    if (job.getString("type").equals("Pod") && !job.getString("templateName").equals("exit-handler")){
                        jobsStatus.put(job.getString("displayName"), job.getString("phase").toUpperCase());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
            job.setStatus(JobStatus.valueOf(jobsStatus.get(job.getName())));
            if (job.getStatus() == JobStatus.SUCCEEDED) {
//            if (success) {
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
        workflow.setEndTime(argoWorkflow.getFinishedat());
        workflow.setStatus(wfStatus);
        workflowRepository.save(workflow);

        return new ResponseEntity<>(workflow, HttpStatus.OK);
    }
}
