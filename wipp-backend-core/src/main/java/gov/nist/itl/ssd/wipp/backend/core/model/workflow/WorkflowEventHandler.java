package gov.nist.itl.ssd.wipp.backend.core.model.workflow;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RepositoryEventHandler(Workflow.class)
public class WorkflowEventHandler {
    // We make sure the user trying to create a workflow is logged in.
    @PreAuthorize("@securityServiceData.hasUserRole()")
    @HandleBeforeCreate
    public void handleBeforeCreate(Workflow workflow) {
        // We set the owner to the connected user
        workflow.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());
        workflow.setCreationDate(new Date());
        workflow.setStatus(WorkflowStatus.CREATED);
    }
}
