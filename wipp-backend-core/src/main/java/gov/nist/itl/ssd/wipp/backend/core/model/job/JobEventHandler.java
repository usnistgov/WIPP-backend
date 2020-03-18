package gov.nist.itl.ssd.wipp.backend.core.model.job;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.Workflow;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
@RepositoryEventHandler(Job.class)
public class JobEventHandler {

    @Autowired
    CoreConfig config;

    @Autowired
    private JobLogic jobLogic;

    @Autowired
    private WorkflowRepository workflowRepository;

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
            // If the Job is created by a workflow, set the job's owner as the workflow's owner
            job.setOwner(workflow.get().getOwner());
        }
        else {
            // Else, set the job owner as the currently logged in user
            job.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());
        }

    }
}
