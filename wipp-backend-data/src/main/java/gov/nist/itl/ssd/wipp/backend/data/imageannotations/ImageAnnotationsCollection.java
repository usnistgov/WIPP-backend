package gov.nist.itl.ssd.wipp.backend.data.imageannotations;

import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualRef;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@IdExposed
@Document
public class ImageAnnotationsCollection {

    @Id
    private String id;

    private String name;

    private String owner;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date creationDate;

    @Indexed(unique = true, sparse = true)
    @ManualRef(Job.class)
    private String sourceJob;

    private String imagesCollectionId;

    private String startMaskCollectionId;

    private String taskId;

    private boolean publiclyShared;

    public ImageAnnotationsCollection() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public void setSourceJob(String sourceJob) {
        this.sourceJob = sourceJob;
    }

    public String getImagesCollectionId() {
        return imagesCollectionId;
    }

    public void setImagesCollectionId(String imagesCollectionId) {
        this.imagesCollectionId = imagesCollectionId;
    }

    public String getStartMaskCollectionId() { return startMaskCollectionId; }

    public void setStartMaskCollectionId(String startMaskCollectionId) { this.startMaskCollectionId = startMaskCollectionId; }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public boolean isPubliclyShared() {
        return publiclyShared;
    }

    public void setPubliclyShared(boolean publiclyShared) {
        this.publiclyShared = publiclyShared;
    }

}
