package gov.nist.itl.ssd.wipp.backend.argo.workflows.workflow;

import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.Workflow;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowRepository;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

/**
 * This class is responsible for doing the security checks on the secured objects.
 * If the object's id is provided, it will retrieve the object itself. If the object doesn't exist, it will throw a NotFoundException
 * If the object is provided, it will check that the object is publicly available or that the owner is the connected user if the object is private
 * If the user is not authorized, it will throw a ForbiddenException
 */

@Service
public class SecurityServiceWorkflow {
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

    public boolean checkAuthorizeWorkflowId(String workflowId){
        Optional<Workflow> workflow = workflowRepository.findById(workflowId);
        if (workflow.isPresent()){
            return(checkAuthorize(workflow.get()));
        }
        else {
            throw new NotFoundException("Workflow with id " + workflowId + " not found");
        }
    }

    public static boolean checkAuthorize(Workflow workflow) {
        String workflowOwner = workflow.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (workflowOwner != null && !workflowOwner.equals(connectedUser)) {
            throw new ForbiddenException("You do not have access to this workflow");
        }
        return(true);
    }

    /**
     * This method is needed to make sure the user is logged in. This is a workaround, because Keycloak's hasRole() method is not working.
     */

    public static boolean hasUserRole(){
        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        for (GrantedAuthority grantedAuthority : authorities) {
            if(grantedAuthority.getAuthority().toString().equals("user")){
                return(true);
            }
        }
        return(false);
    }
}