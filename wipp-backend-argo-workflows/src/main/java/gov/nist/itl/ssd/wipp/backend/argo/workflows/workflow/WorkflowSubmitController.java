package gov.nist.itl.ssd.wipp.backend.argo.workflows.workflow;

import gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin.Plugin;
import gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin.PluginRepository;
import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.Workflow;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowStatus;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import io.swagger.annotations.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    private static final Logger LOGGER = Logger.getLogger(WorkflowSubmitController.class.getName());

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

			String workflowFilePath = workflowFolder + File.separator + "workflow-" + workflowId + ".yaml";
			converter.convert(workflow, jobsDependencies, jobsPlugins,workflowFilePath);
			
			// Launch submission of the workflow to Argo
			workflow = executeSubmissionCommand(workflow, workflowFilePath);
            workflow.setStatus(WorkflowStatus.SUBMITTED);

            // Save the workflow and send the HTTP response
            workflowRepository.save(workflow);
            return new ResponseEntity<>(workflow, HttpStatus.OK);
            
        } catch (Exception ex) {
        	workflow.setStatus(WorkflowStatus.ERROR);
        	workflow.setErrorMessage(ex.getMessage());
        	workflowRepository.save(workflow);
            LOGGER.log(Level.SEVERE, "Cannot start workflow: " + ex.getMessage());
        	throw new ClientException("Error while submitting workflow: " + ex.getMessage());
        }

    }
    
    /**
     * Execute Argo workflow submission command
     * @param workflow
     * @param workflowFilePath
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws RuntimeException
     */
	private Workflow executeSubmissionCommand(Workflow workflow, String workflowFilePath)
			throws IOException, InterruptedException, RuntimeException {
        // Build Argo command
    	List<String> builderCommands = new ArrayList<>();
        Collections.addAll(builderCommands, config.getWorkflowBinary().split(" "));
        builderCommands.add("submit");
        builderCommands.add("--output");
        builderCommands.add("name");
        builderCommands.add(workflowFilePath);
        
        ProcessBuilder builder = new ProcessBuilder(builderCommands);
        builder.redirectInput(Redirect.INHERIT);
        Process process;
        
        // Submit workflow to Argo
        process = builder.start();
        int exitCode = process.waitFor();
    	
        // if Argo exit code is zero, submission was successful, get generated name
    	if (exitCode == 0) {
    		InputStream is = process.getInputStream();
        	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        	String line = reader.readLine();
        	if(line != null){
        		workflow.setGeneratedName(line);
        	} 
        // else submission failed, get error message
    	} else {
    		InputStream es = process.getErrorStream();
    		BufferedReader errorReader = new BufferedReader(new InputStreamReader(es));
    		String errorMsg = errorReader.readLine();
    		throw new RuntimeException(errorMsg);
    	}
    	
    	return workflow;
    }
}
