package gov.nist.itl.ssd.wipp.backend.core.model.workflow;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RepositoryEventHandler(Workflow.class)
public class WorkflowEventHandler {
    @HandleBeforeCreate
    public void handleBeforeCreate(Workflow workflow) {
        workflow.setCreationDate(new Date());
        workflow.setStatus(WorkflowStatus.CREATED);
    }
}
