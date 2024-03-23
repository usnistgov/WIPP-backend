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

package gov.nist.itl.ssd.wipp.backend.core.model.workflow;


import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobStatus;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Workflow copy controller 
 * 
 * @author Samia Benjida <samia.benjida at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Controller
@Tag(name="Workflow Entity")
@RequestMapping(CoreConfig.BASE_URI + "/workflows/{workflowId}/copy")
public class WorkflowCopyController
{

    @Autowired
    private JobRepository<Job> jobRepository;

    @Autowired
    private WorkflowRepository workflowRepository;
    
    @Autowired
    private WorkflowLogic workflowLogic;

    @PreAuthorize("isAuthenticated() and "
    		+ "(hasRole('admin') or @workflowSecurity.checkAuthorize(#workflowId, false))")
    @RequestMapping(
            value = "",
            method = RequestMethod.POST)  
    public ResponseEntity<Workflow> copy(
            @PathVariable("workflowId") String workflowId,
            @RequestBody String workflowName) {
    	
    	if (workflowName == null) {
            throw new ClientException(
                    "A name for the new workflow must be provided.");
        }
        workflowLogic.assertWorkflowNameUnique(workflowName);
        
        // Get the base workflow
        Optional<Workflow> w = workflowRepository.findById(
        		workflowId);
        if (!w.isPresent()) {
            throw new ResourceNotFoundException(
                    "Workflow " + workflowId + " not found.");
        }
        Workflow workflow = w.get();
        
        // Create the copied workflow
        Workflow copy = new Workflow(workflowName);
        copy.setStatus(WorkflowStatus.CREATED);
        // Set the owner to the connected user
        copy.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());
        copy = workflowRepository.save(copy);

        try {

            // Add the list of job of the base workflow in the copied workflow
            List<Job> jobList = jobRepository.findByWippWorkflow(workflowId);
            // Map the base job ids to their corresponding new job ids
            Map<String, String> dependenciesMapping = new HashMap<>();

            // create all the new jobs
            for (Job job : jobList) {
                Job copiedJob = new Job();
                copiedJob.setOwner(copy.getOwner());
                copiedJob.setName(job.getName().replace(workflow.getName(), workflowName));
                copiedJob.setStatus(JobStatus.CREATED);
                copiedJob.setCreationDate(new Date());
                copiedJob.setWippWorkflow(copy.getId());
                copiedJob.setWippExecutable(job.getWippExecutable());
                copiedJob = jobRepository.save(copiedJob);
                dependenciesMapping.put(job.getId(), copiedJob.getId());
            }

            // populate the jobs
            for (Job job: jobList) {
                Job copiedJob = jobRepository.findById(dependenciesMapping.get(job.getId())).get();
                
                List<String> dependencies = new ArrayList<>();
                // set the output to null for the new jobs
                Map<String, String> outputs = job.getOutputParameters();
                for (String output : outputs.keySet()) {
                    outputs.put(output, null);
                }
                copiedJob.setOutputParameters(outputs);

                // change the linked inputs and dependencies ids
                Map<String, String> inputs = job.getParameters();
                for (String input : inputs.keySet()) {
                    String regex = "\\{\\{ (.*)\\.(.*) \\}\\}";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher m = pattern.matcher(inputs.get(input));
                    if (m.find()) {
                        String jobId = m.group(1);
                        String outputName = m.group(2);
                        String matchingId = dependenciesMapping.get(jobId);
                        dependencies.add(matchingId);
                        inputs.put(input, inputs.get(input).replace(jobId, matchingId));
                    }
                }
                copiedJob.setParameters(inputs);
                copiedJob.setDependencies(dependencies);
                jobRepository.save(copiedJob);
            }

            copy = workflowRepository.save(copy);
            return new ResponseEntity<>(copy, HttpStatus.OK);
            
        } catch (Exception e) {
        	jobRepository.deleteByWippWorkflow(copy.getId());
        	workflowRepository.delete(copy);
            throw new ClientException("Error while copying workflow" + e);
        }
    }

}
