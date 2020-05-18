package gov.nist.itl.ssd.wipp.backend.core.model.workflow;

import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobRepository;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * This class is responsible for doing the security checks on the secured objects.
 * If the object doesn't exist, it will throw a NotFoundException
 * If the object exists, it will check that the the user has the permission to access the object
 * If the user is not authorized, it will throw a ForbiddenException
 */

@Service
public class WorkflowSecurity {
    @Autowired
    private WorkflowRepository workflowRepository;
    @Autowired
    private JobRepository jobRepository;

    public boolean checkAuthorizeJobId(String jobId){
        Optional<Job> job = jobRepository.findById(jobId);
        if (job.isPresent()){
            return(checkAuthorize(job.get()));
        }
        else {
            throw new NotFoundException("Job with id " + jobId + " not found");
        }
    }
    public boolean checkAuthorize(Job job){
        String jobOwner = job.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (jobOwner != null && !jobOwner.equals(connectedUser)) {
            throw new ForbiddenException("You do not have access to this job");
        }
        return(true);
    }

    public boolean checkAuthorize(String workflowId, Boolean editMode) {
        Optional<Workflow> workflow = workflowRepository.findById(workflowId);
        if (workflow.isPresent()){
            return(checkAuthorize(workflow.get(), editMode));
        }
        else {
            throw new NotFoundException("Workflow with id " + workflowId + " not found");
        }
    }

    public static boolean checkAuthorize(Workflow workflow, Boolean editMode) {
        String workflowOwner = workflow.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!workflow.isPubliclyShared() && (workflowOwner == null || !workflowOwner.equals(connectedUser))) {
            throw new ForbiddenException("You do not have access to this Workflow");
        }
        if (workflow.isPubliclyShared() && editMode && (workflowOwner == null || !workflowOwner.equals(connectedUser))){
            throw new ForbiddenException("You do not have the permission to edit this Workflow");
        }
        return(true);
    }
}