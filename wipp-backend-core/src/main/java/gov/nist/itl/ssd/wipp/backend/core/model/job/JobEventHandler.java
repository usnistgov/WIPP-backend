package gov.nist.itl.ssd.wipp.backend.core.model.job;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RepositoryEventHandler(Job.class)
public class JobEventHandler {

    @Autowired
    CoreConfig config;

    @Autowired
    private JobLogic jobLogic;

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

    }
}
