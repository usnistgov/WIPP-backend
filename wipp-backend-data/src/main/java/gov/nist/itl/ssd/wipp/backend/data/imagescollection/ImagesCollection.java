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
package gov.nist.itl.ssd.wipp.backend.data.imagescollection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import gov.nist.itl.ssd.wipp.backend.core.model.data.Data;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualRef;
import gov.nist.itl.ssd.wipp.backend.data.SecurityService;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

//import gov.nist.itl.ssd.fes.job.Job;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * Adapted by Mohamed Ouladi <mohamed.ouladi@nist.gov>
 */
@IdExposed
@Document
public class ImagesCollection extends Data {

    @Id
    private String id;

    private String name;

    private String owner;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date creationDate;

    @Indexed(unique = true, sparse = true)
    @ManualRef(Job.class)
    private String sourceJob;
    
    private String sourceCatalog;
    
    private ImagesCollectionImportMethod importMethod;

    private boolean locked;

    private boolean publiclyAvailable;

    private String pattern;

    private String notes;

    @JsonProperty(access = Access.READ_ONLY)
    private int numberOfImages;

    @JsonProperty(access = Access.READ_ONLY)
    private long imagesTotalSize;

    @JsonProperty(access = Access.READ_ONLY)
    private int numberImportingImages;

    @JsonProperty(access = Access.READ_ONLY)
    private int numberOfImportErrors;

    @JsonProperty(access = Access.READ_ONLY)
    private int numberOfMetadataFiles;

    @JsonProperty(access = Access.READ_ONLY)
    private long metadataFilesTotalSize;
    public ImagesCollection() {
    }

    public ImagesCollection(String name) {
        this(name, false, ImagesCollectionImportMethod.UPLOADED);
    }
    
    public ImagesCollection(String name, boolean locked, ImagesCollectionImportMethod importMethod){
        this.name = name;
        this.locked = locked;
        this.creationDate = new Date();	
        this.importMethod = importMethod;
    }

    public ImagesCollection(Job job, String outputName) {
        this.name = job.getName() + "-" + outputName;
        this.sourceJob = job.getId();
        this.locked = true;
        this.creationDate = new Date();
        this.importMethod = ImagesCollectionImportMethod.JOB;
    }
    
    public ImagesCollection(String name, String sourceCatalog){
        this.name = name;
        this.locked = true;
        this.creationDate = new Date();	
        this.sourceCatalog = sourceCatalog;
        this.importMethod = ImagesCollectionImportMethod.CATALOG;
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

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getSourceJob() {
        if(SecurityService.checkAuthorize(this)){
            return sourceJob;
        }
        else {
            return null;
        }
    }

    public boolean isLocked() {
        if(SecurityService.checkAuthorize(this)){
            return locked;
        }
        else {
            return false;
        }
    }

	public String getPattern() {
        if(SecurityService.checkAuthorize(this)){
            return pattern;
        }
        else {
            return null;
        }
	}

	public String getNotes() { return notes; }


    public void setNotes(String notes) {
        this.notes = notes;
    }

	public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getNumberOfImages() {
        if(SecurityService.checkAuthorize(this)){
            return numberOfImages;
        }
        else {
            return 0;
        }
    }

    public long getImagesTotalSize() {
        if(SecurityService.checkAuthorize(this)){
            return imagesTotalSize;
        }
        else {
            return 0;
        }
    }

    public int getNumberImportingImages() {
        if(SecurityService.checkAuthorize(this)){
            return numberImportingImages;
        }
        else {
            return 0;
        }
    }

    public int getNumberOfImportErrors() {
        if(SecurityService.checkAuthorize(this)){
            return numberOfImportErrors;
        }
        else {
            return 0;
        }
    }

    public int getNumberOfMetadataFiles() {
        if(SecurityService.checkAuthorize(this)){
            return numberOfMetadataFiles;
        }
        else {
            return 0;
        }
    }

    public long getMetadataFilesTotalSize() {
        if(SecurityService.checkAuthorize(this)){
            return metadataFilesTotalSize;
        }
        else {
            return 0;
        }
    }

    public String getOwner() {

        return owner;
    }

    public void setOwner(String owner) { this.owner = owner; }

    public boolean isPubliclyAvailable() {
        return publiclyAvailable;
    }

    public void setPubliclyAvailable(boolean publiclyAvailable) { this.publiclyAvailable = publiclyAvailable; }

	public String getSourceCatalog() {
		return sourceCatalog;
	}

	public ImagesCollectionImportMethod getImportMethod() {
		return importMethod;
	}

	public void setImportMethod(ImagesCollectionImportMethod importMethod) {
		this.importMethod = importMethod;
	}
	
    public enum ImagesCollectionImportMethod {UPLOADED, JOB, CATALOG}

}
