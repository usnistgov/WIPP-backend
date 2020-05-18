/*
 * This software was developed at the National Institute of Standards and
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
 */
package gov.nist.itl.ssd.wipp.backend.core.model.job;

import java.util.Date;
import java.util.Map;
import java.util.List;

import gov.nist.itl.ssd.wipp.backend.core.model.workflow.Workflow;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualListRef;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualRef;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.Updatable;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * @author Philippe Dessauw <philippe.dessauw at nist.gov>
 */
@IdExposed
@Document(collection = "job")
public class Job {
    @Id
    private String id;

    private String name;

    private String owner;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date creationDate;

    @Updatable
    private JobStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date endTime;

    private String error;

    // FIXME ManualRef not working on abstract class
    // @ManualRef(value = Computation.class)
    private String wippExecutable;

    @ManualListRef(value = Job.class)
    private List<String> dependencies;

    private Map<String, String> parameters;

    private Map<String, String> outputParameters;

    @ManualRef(value = Workflow.class)
    private String wippWorkflow;

    private String wippVersion;
    
    private boolean publiclyShared;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameter(String key) {
        return this.parameters.get(key);
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getWippExecutable() {
		return wippExecutable;
	}

	public void setWippExecutable(String wippExecutable) {
		this.wippExecutable = wippExecutable;
	}

	public String getWippWorkflow() {
		return wippWorkflow;
	}

	public void setWippWorkflow(String wippWorkflow) {
		this.wippWorkflow = wippWorkflow;
	}

	public String getWippVersion() {
		return wippVersion;
	}

	public void setWippVersion(String wippVersion) {
		this.wippVersion = wippVersion;
	}

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public Map<String, String> getOutputParameters() {
        return outputParameters;
    }

    public void setOutputParameters(Map<String, String> outputParameters) {
        this.outputParameters = outputParameters;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

	public boolean isPubliclyShared() {
		return publiclyShared;
	}

	public void setPubliclyShared(boolean publiclyShared) {
		this.publiclyShared = publiclyShared;
	}
}