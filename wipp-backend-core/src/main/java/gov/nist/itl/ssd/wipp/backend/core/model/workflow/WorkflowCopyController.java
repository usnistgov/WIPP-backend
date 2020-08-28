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
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Samia Benjida <samia.benjida at nist.gov>
 */
@Controller
@Api(tags="Workflow Entity")
@RequestMapping(CoreConfig.BASE_URI + "/workflows/{workflowName}/copy")
public class WorkflowCopyController
{

    @Autowired
    private JobRepository<Job> jobRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @RequestMapping(
            value = "",
            method = RequestMethod.POST
    )  public ResponseEntity<Workflow> copy(
            @PathVariable("workflowName") String workflowName,
            @RequestBody String workflowId
    ) {
        try {
            // Get the base workflow
            Workflow workflow = workflowRepository.findById(workflowId).get();

            // Create the copied workflow and save it to create an ID
            Workflow newWorkflow = new Workflow();
            newWorkflow = workflowRepository.save(newWorkflow);
            newWorkflow.setStatus(WorkflowStatus.CREATED);
            newWorkflow.setCreationDate(new Date());
            newWorkflow.setName(workflowName);

            // Add the list of job of the base workflow in the copied workflow
            List<Job> jobList = jobRepository.findByWippWorkflow(workflowId);
            // Map the base job ids to their corresponding new job ids
            Map<String, String> dependenciesMapping = new HashMap<>();

            // create all the new jobs
            for (Job job : jobList) {
                Job copiedJob = new Job();
                copiedJob = jobRepository.save(copiedJob);
                dependenciesMapping.put(job.getId(), copiedJob.getId());
            }

            // populate the jobs
            for (Job job: jobList) {
                Job copiedJob = jobRepository.findById(dependenciesMapping.get(job.getId())).get();
                copiedJob.setName(job.getName().replace(workflow.getName(), workflowName));
                copiedJob.setStatus(JobStatus.CREATED);
                copiedJob.setCreationDate(new Date());
                copiedJob.setWippWorkflow(newWorkflow.getId());
                copiedJob.setWippExecutable(job.getWippExecutable());
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

            newWorkflow = workflowRepository.save(newWorkflow);
            return new ResponseEntity<>(newWorkflow, HttpStatus.OK);
        } catch (Exception e) {
            throw new ClientException("Error while copying workflow" + e);
        }
    }

}
