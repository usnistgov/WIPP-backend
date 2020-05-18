package gov.nist.itl.ssd.wipp.backend.data.tensorboard;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualRef;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
*/
@IdExposed
@Document
public class TensorboardLogs {
	
    @Id
    private String id;

	private String owner;

    @Indexed(unique = true)
    private String name;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date creationDate;
    
    @Indexed(unique = true, sparse = true)
    @ManualRef(Job.class)
    private String sourceJob;

	private boolean publiclyShared;
    
    public TensorboardLogs(){	
    }

	public TensorboardLogs(String name){
		this.name = name;
		this.creationDate = new Date();
	}
	
	public TensorboardLogs(Job job, String outputName){
		this.name = job.getName() + "-" + outputName;
		this.sourceJob = job.getId();
		this.creationDate = new Date();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getSourceJob() {
		return sourceJob;
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
