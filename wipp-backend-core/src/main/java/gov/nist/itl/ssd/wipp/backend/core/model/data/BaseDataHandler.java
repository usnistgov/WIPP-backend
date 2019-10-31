package gov.nist.itl.ssd.wipp.backend.core.model.data;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;



public abstract class BaseDataHandler {

    @Autowired
    CoreConfig config;

    protected final File getJobOutputTempFolder(String jobId, String outputName) {
        return new File(new File(config.getJobsTempFolder(), jobId), outputName);
    }

    protected void setOutputId(Job job, String outputName, String id) {
        job.getOutputParameters().put(outputName, id);
    }

}
