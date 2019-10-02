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
package gov.nist.itl.ssd.wipp.backend.data.stitching;

import gov.nist.itl.ssd.wipp.backend.data.stitching.timeslices.StitchingVectorTimeSlice;
import gov.nist.itl.ssd.wipp.backend.core.model.data.Data;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualRef;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

/**
*
* @author Antoine Vandecreme
*/
@Document
@IdExposed
public class StitchingVector extends Data {

    @Id
    private String id;

    private String name;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date creationDate;

    private String tilesPattern;

    @JsonIgnore
    private List<StitchingVectorTimeSlice> timeSlices;

    @Indexed(unique = true, sparse = true)
    @ManualRef(Job.class)
    private String job;

    public StitchingVector() {
    }

    StitchingVector(String name, String tilesPattern,
            List<StitchingVectorTimeSlice> timeSlices) {
        this.name = name;
        this.tilesPattern = tilesPattern;
        this.timeSlices = timeSlices;
        this.creationDate = new Date();
    }

    public StitchingVector(Job job,
            List<StitchingVectorTimeSlice> timeSlices) {
        this.name = job.getName();
        //StitchingOptions stitchingOptions = job.get;
//        this.tilesPattern = stitchingOptions != null
//                ? stitchingOptions.getFilenamePattern() : null;
        this.job = job.getId();
        this.creationDate = new Date();
        this.timeSlices = timeSlices;
    }

    StitchingVector(Job job, List<StitchingVectorTimeSlice> timeSlices, String outputName) {
    	this.name = job.getName() + "-" + outputName;
    	this.job = job.getId();
        this.creationDate = new Date();
        this.timeSlices = timeSlices;
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

    public String getTilesPattern() {
        return tilesPattern;
    }

    public List<StitchingVectorTimeSlice> getTimeSlices() {
        // For backward compatibility
        if (timeSlices == null) {
            return Arrays.asList(new StitchingVectorTimeSlice(1, ""));
        }
        return timeSlices;
    }

    public int getNumberOfTimeSlices() {
        return getTimeSlices().size();
    }

    @JsonIgnore
    public String getStitchingJob() {
        return job;
    }
}

