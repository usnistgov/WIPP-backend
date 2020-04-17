package gov.nist.itl.ssd.wipp.backend.data.pyramidannotation;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.nist.itl.ssd.wipp.backend.core.model.data.Data;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualRef;
import gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.timeslices.PyramidAnnotationTimeSlice;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@Document
@IdExposed
public class PyramidAnnotation extends Data {
	
    @Id
    private String id;
    
	private final String name;
	
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date creationDate;
    
    @JsonIgnore
    private List<PyramidAnnotationTimeSlice> timeSlices;

    @Indexed(unique = true, sparse = true)
    @ManualRef(Job.class)
    private String job;
    
    public PyramidAnnotation() {
    }

    PyramidAnnotation(String name, List<PyramidAnnotationTimeSlice> timeSlices) {
        this.name = name;
        this.timeSlices = timeSlices;
        this.creationDate = new Date();
    }

    public PyramidAnnotation(Job job,
            List<PyramidAnnotationTimeSlice> timeSlices) {
        this.name = job.getName();
        this.job = job.getId();
        this.creationDate = new Date();
        this.timeSlices = timeSlices;
    }

    PyramidAnnotation(Job job, List<PyramidAnnotationTimeSlice> timeSlices, String outputName) {
    	this.name = job.getName() + "-" + outputName;
    	this.job = job.getId();
        this.creationDate = new Date();
        this.timeSlices = timeSlices;
    }
    

    public String getName() {
        return name;
    }
    
    public String getId() {
        return id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public List<PyramidAnnotationTimeSlice> getTimeSlices() {
        // For backward compatibility
        if (timeSlices == null) {
            return Arrays.asList(new PyramidAnnotationTimeSlice(1));
        }
        return timeSlices;
    }

    public int getNumberOfTimeSlices() {
        return getTimeSlices().size();
    }

    @JsonIgnore
    public String getPyramidAnnotationJob() {
        return job;
    }
}
