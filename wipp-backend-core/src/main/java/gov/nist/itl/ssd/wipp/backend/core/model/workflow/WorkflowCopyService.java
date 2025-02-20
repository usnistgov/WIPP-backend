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

import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobStatus;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Workflow copy service
 *
 * @author Samia Benjida <samia.benjida at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Service
public class WorkflowCopyService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowLogic workflowLogic;

    public Workflow copy(String workflowId, String workflowName, String owner, WorkflowStatus status) {

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
        copy.setStatus(status);
        // Set the owner to the connected/selected user
        copy.setOwner(owner);
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

            return copy = workflowRepository.save(copy);

        } catch (Exception e) {
            jobRepository.deleteByWippWorkflow(copy.getId());
            workflowRepository.delete(copy);
            throw e;
        }
    }
}
