package gov.nist.itl.ssd.wipp.backend.core.model.job;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.Workflow;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowStatus;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */

@Component
@RepositoryEventHandler(Job.class)
public class JobEventHandler {

    @Autowired
    CoreConfig config;

    @Autowired
    private JobLogic jobLogic;
    
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private WorkflowRepository workflowRepository;
    
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or @workflowSecurity.checkAuthorize(#job.wippWorkflow, true))")
    @HandleBeforeCreate
    public void handleBeforeCreate(Job job) {

        jobLogic.assertNotNull(job.getName());
        jobLogic.assertJobIdNull(job.getId());
        jobLogic.assertJobNameUnique(job.getName());
        jobLogic.assertNotNull(job.getWippExecutable());

        job.setWippVersion(config.getWippVersion());
        job.setStatus(JobStatus.CREATED);
        job.setError(null);
        job.setCreationDate(new Date());
        job.setStartTime(null);
        job.setEndTime(null);
        Optional<Workflow> workflow = workflowRepository.findById(job.getWippWorkflow());
        if (workflow.isPresent()){
            job.setOwner(workflow.get().getOwner());
        } else {
        	throw new NotFoundException("Workflow with id " + job.getWippWorkflow() + " not found");
        }
        if (workflow.get().getStatus() != WorkflowStatus.CREATED) {
            throw new ClientException("Can not add job to workflow that is not in CREATION mode");
        }

    }
    
    @HandleBeforeSave
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #job.owner == principal.name)")
    public void handleBeforeSave(Job job) {
    	// Assert job exists
        Optional<Job> result = jobRepository.findById(
        		job.getId());
    	if (!result.isPresent()) {
        	throw new NotFoundException("Job with id " + job.getId() + " not found");
        }

        Job oldJ = result.get();

    	// A public job cannot become private
    	if (oldJ.isPubliclyShared() && !job.isPubliclyShared()){
            throw new ClientException("Can not change a public job to private.");
        }
    	
    	// Owner cannot be changed
        if (!Objects.equals(
        		job.getOwner(),
                oldJ.getOwner())) {
            throw new ClientException("Can not change owner.");
        }

    	// Creation date cannot be changed
        if (!Objects.equals(
        		job.getCreationDate(),
                oldJ.getCreationDate())) {
            throw new ClientException("Can not change creation date.");
        }
        
        // Only jobs with status "CREATED" can be modified
        jobLogic.assertStatusIsCreated(oldJ);

    }
    
    @HandleBeforeDelete
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #job.owner == principal.name)")
    public void handleBeforeDelete(Job job) {
    	// Assert job exists
    	Optional<Job> result = jobRepository.findById(
                job.getId());
    	if (!result.isPresent()) {
        	throw new NotFoundException("Job with id " + job.getId() + " not found");
        }

        Job oldJob = result.get();
        
        // Only jobs with status "CREATED" can be deleted
        jobLogic.assertStatusIsCreated(oldJob);
    }
}
