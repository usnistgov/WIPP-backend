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
package gov.nist.itl.ssd.wipp.backend.images.imagescollection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import gov.nist.itl.ssd.wipp.backend.core.model.data.WippData;
import gov.nist.itl.ssd.wipp.backend.core.model.job.WippJob;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualRef;

//import gov.nist.itl.ssd.fes.job.Job;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * Adapted by Mohamed Ouladi <mohamed.ouladi@nist.gov>
 */
@IdExposed
@Document
public class ImagesCollection extends WippData {

    @Id
    private String id;

    private String name;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date creationDate;

    @Indexed(unique = true, sparse = true)
    @ManualRef(WippJob.class)
    private String sourceJob;

    private boolean locked;
    
    private UploadOption uploadOption;
    
    private String pattern;

    @JsonIgnore
    private int numberOfImages;

    @JsonIgnore
    private long imagesTotalSize;

    @JsonIgnore
    private int numberImportingImages;

    @JsonIgnore
    private int numberOfImportErrors;

    @JsonIgnore
    private int numberOfMetadataFiles;

    @JsonIgnore
    private long metadataFilesTotalSize;

    public ImagesCollection() {
    }

    public ImagesCollection(String name) {
        this(name, false);
    }

    public ImagesCollection(String name, boolean locked) {
        this.name = name;
        this.locked = locked;
        this.creationDate = new Date();
    }

    public ImagesCollection(WippJob job) {
        this.name = job.getName();
        this.sourceJob = job.getId();
        this.locked = true;
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

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getSourceJob() {
        return sourceJob;
    }

    public boolean isLocked() {
        return locked;
    }

    public UploadOption getUploadOption() {
		return uploadOption;
	}

	public String getPattern() {
		return pattern;
	}

	public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @JsonProperty
    public int getNumberOfImages() {
        return numberOfImages;
    }

    @JsonProperty
    public long getImagesTotalSize() {
        return imagesTotalSize;
    }

    @JsonProperty
    public int getNumberImportingImages() {
        return numberImportingImages;
    }

    @JsonProperty
    public int getNumberOfImportErrors() {
        return numberOfImportErrors;
    }

    @JsonProperty
    public int getNumberOfMetadataFiles() {
        return numberOfMetadataFiles;
    }

    @JsonProperty
    public long getMetadataFilesTotalSize() {
        return metadataFilesTotalSize;
    }
    
    public enum UploadOption {
    	REGULAR,
    	IGNORE_SUBS,
    	INCLUDE_PATH_IMG_NAME
    }
}
