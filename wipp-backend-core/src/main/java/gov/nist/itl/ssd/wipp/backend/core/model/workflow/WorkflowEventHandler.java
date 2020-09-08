package gov.nist.itl.ssd.wipp.backend.core.model.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Component
@RepositoryEventHandler(Workflow.class)
public class WorkflowEventHandler {
	
	@Autowired
	WorkflowRepository workflowRepository;
	
	@Autowired
    private WorkflowLogic workflowLogic;

	@PreAuthorize("isAuthenticated()")
    @HandleBeforeCreate
    public void handleBeforeCreate(Workflow workflow) {
		// Assert workflow name is unique
		workflowLogic.assertWorkflowNameUnique(
				workflow.getName());
        
        // Set the owner to the connected user
        workflow.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());
        
        // Set creation date to current date
        workflow.setCreationDate(new Date());
        
        // Set status to CREATED
        workflow.setStatus(WorkflowStatus.CREATED);
    }
	
	@HandleBeforeSave
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #workflow.owner == principal.name)")
    public void handleBeforeSave(Workflow workflow) {
    	// Assert workflow exists
        Optional<Workflow> result = workflowRepository.findById(
        		workflow.getId());
    	if (!result.isPresent()) {
        	throw new NotFoundException("Workflow with id " + workflow.getId() + " not found");
        }

        Workflow oldW = result.get();

    	// A public workflow cannot become private
    	if (oldW.isPubliclyShared() && !workflow.isPubliclyShared()){
            throw new ClientException("Can not change a public workflow to private.");
        }
    	
    	// Owner cannot be changed
        if (!Objects.equals(
        		workflow.getOwner(),
                oldW.getOwner())) {
            throw new ClientException("Can not change owner.");
        }

    	// Creation date cannot be changed
        if (!Objects.equals(
        		workflow.getCreationDate(),
                oldW.getCreationDate())) {
            throw new ClientException("Can not change creation date.");
        }

    }
}
