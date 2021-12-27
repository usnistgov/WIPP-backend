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
package gov.nist.itl.ssd.wipp.backend.data.genericdata;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;

import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualRef;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
*/
@IdExposed
@Document
public class GenericData {

	@Id
	private String id;

	@Indexed(unique=true)
	private String name;
	
	private boolean locked;
	
	private String owner;
	
	private String type;
	
	private String description;
	
	private String metadata;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private int numberOfFiles;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private long fileTotalSize;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private Date creationDate;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private int numberOfImportErrors;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private int numberImportingGenericFiles;

	@Indexed(unique = true, sparse = true)
	@ManualRef(Job.class)
	private String sourceJob;
	
    private boolean publiclyShared;
	
	public GenericData() {
	}

	public GenericData(String name, boolean locked){
		this.name = name;
		this.creationDate = new Date();
		this.locked = locked;
	}

	public GenericData(Job job){
		this.name = job.getName();
		this.sourceJob = job.getId();
		this.creationDate = new Date();
	}

	public GenericData(Job job, String outputName) {
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
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public String getSourceJob() {
		return sourceJob;
	}

	public int getNumberOfFiles() {
		return numberOfFiles;
	}

	public long getFileTotalSize() {
		return fileTotalSize;
	}
	
	public boolean isPubliclyShared() {
        return publiclyShared;
    }

    public void setPubliclyShared(boolean publiclyShared) { 
    	this.publiclyShared = publiclyShared; 
    }
    
	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {this.locked = locked;}

	public int getNumberOfImportErrors() {
		return numberOfImportErrors;
	}

	public int getNumberImportingGenericFiles() {
		return numberImportingGenericFiles;
	}
	
}
