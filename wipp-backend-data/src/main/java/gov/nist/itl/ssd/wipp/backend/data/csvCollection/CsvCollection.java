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
package gov.nist.itl.ssd.wipp.backend.data.csvCollection;

import java.util.Date;

import gov.nist.itl.ssd.wipp.backend.data.SecurityService;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualRef;

/**
 *
 * @author Mohamed Ouladi <mohamed.ouladi@nist.gov>
 */
@IdExposed
@Document
public class CsvCollection {

	@Id
	private String id;

	private String name;

	private String owner;

	private boolean publiclyAvailable;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private Date creationDate;

	@Indexed(unique = true, sparse = true)
	@ManualRef(Job.class)
	private String sourceJob;


	public CsvCollection() {
	}

	public CsvCollection(String name){
		this.name = name;
		this.creationDate = new Date();
	}

	public CsvCollection(Job job){
		this.name = job.getName();
		this.sourceJob = job.getId();
		this.creationDate = new Date();
	}

	public CsvCollection(Job job, String outputName) {
		this.name = job.getName() + "-" + outputName;
		this.sourceJob = job.getId();
		this.creationDate = new Date();
	}

	public String getId() {
		if(SecurityService.checkAuthorize(this)){
			return id;
		}
		else {
			return null;
		}
	}

	public String getName() {
		if(SecurityService.checkAuthorize(this)){
			return name;
		}
		else {
			return null;
		}
	}

	public Date getCreationDate() {
		if(SecurityService.checkAuthorize(this)){
			return creationDate;
		}
		else {
			return null;
		}
	}

	public String getSourceJob() {
		if(SecurityService.checkAuthorize(this)){
			return sourceJob;
		}
		else {
			return null;
		}
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public boolean isPubliclyAvailable() {
		return publiclyAvailable;
	}

	public void setPubliclyAvailable(boolean publiclyAvailable) {
		this.publiclyAvailable = publiclyAvailable;
	}
}
