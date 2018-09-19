package gov.nist.itl.ssd.wipp.backend.core.model.job;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RepositoryEventHandler(Job.class)
public class JobEventHandler {
    @HandleBeforeCreate
    public void handleBeforeCreate(Job job) {
        job.setCreationDate(new Date());
        job.setStatus(JobStatus.CREATED);
    }
}
