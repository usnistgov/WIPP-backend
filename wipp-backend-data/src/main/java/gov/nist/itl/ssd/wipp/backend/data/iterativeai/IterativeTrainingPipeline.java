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
package gov.nist.itl.ssd.wipp.backend.data.iterativeai;

import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * Iterative AI training pipeline
 * @author Mylene Simon <mylene.simon at nist.gov
 * */
@IdExposed
@Document
public class IterativeTrainingPipeline {

    @Id
    private String id;
    private String name;
    private String owner;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date creationDate;
    private TaskCategory taskCategory;
    // Training data - input
    private String trainingCollection;
    // Training data - ground truth/expected output
    private String groundTruthCollection;
    // Existing ground truth data to use to bootstrap training
    private String startGroundTruthCollection;
    private List<TrainingIteration> iterations;
    private int imagesPerIteration;
    private List<AnnotationColorLabel> labels;
    private List<String> annotatedImages;
    private String workflowTemplate;
    private int currentIteration;
    private boolean publiclyShared;

    public IterativeTrainingPipeline() {
        this.currentIteration = 0;
    }

    public String getTrainingCollection() {
        return trainingCollection;
    }

    public void setTrainingCollection(String trainingCollection) {
        this.trainingCollection = trainingCollection;
    }

    public String getGroundTruthCollection() {
        return groundTruthCollection;
    }

    public void setGroundTruthCollection(String groundTruthCollection) {
        this.groundTruthCollection = groundTruthCollection;
    }

    public List<TrainingIteration> getIterations() {
        return iterations;
    }

    public void setIterations(List<TrainingIteration> iterations) {
        this.iterations = iterations;
    }

    public TaskCategory getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(TaskCategory taskCategory) {
        this.taskCategory = taskCategory;
    }

    public int getImagesPerIteration() {
        return imagesPerIteration;
    }

    public void setImagesPerIteration(int imagesPerIteration) {
        this.imagesPerIteration = imagesPerIteration;
    }

    public List<AnnotationColorLabel> getLabels() {
        return labels;
    }

    public void setLabels(List<AnnotationColorLabel> labels) {
        this.labels = labels;
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

    public String getWorkflowTemplate() {
        return workflowTemplate;
    }

    public void setWorkflowTemplate(String workflowTemplate) {
        this.workflowTemplate = workflowTemplate;
    }

    public List<String> getAnnotatedImages() {
        return annotatedImages;
    }

    public void setAnnotatedImages(List<String> annotatedImages) {
        this.annotatedImages = annotatedImages;
    }

    public boolean isPubliclyShared() {
        return publiclyShared;
    }

    public void setPubliclyShared(boolean publiclyShared) {
        this.publiclyShared = publiclyShared;
    }

    public int getCurrentIteration() {
        return currentIteration;
    }

    public void setCurrentIteration(int currentIteration) {
        this.currentIteration = currentIteration;
    }

    public String getStartGroundTruthCollection() {
        return startGroundTruthCollection;
    }

    public void setStartGroundTruthCollection(String startGroundTruthCollection) {
        this.startGroundTruthCollection = startGroundTruthCollection;
    }

    public static class TrainingIteration {
        private int iterationNumber;
        private String imageAnnotationsCollection;
        private String trainingWorkflow;

        private IterationStatus status;

        public TrainingIteration() {}

        public int getIterationNumber() {
            return iterationNumber;
        }

        public void setIterationNumber(int iterationNumber) {
            this.iterationNumber = iterationNumber;
        }

        public String getTrainingWorkflow() {
            return trainingWorkflow;
        }

        public void setTrainingWorkflow(String trainingWorkflow) {
            this.trainingWorkflow = trainingWorkflow;
        }

        public String getImageAnnotationsCollection() {
            return imageAnnotationsCollection;
        }

        public void setImageAnnotationsCollection(String imageAnnotationsCollection) {
            this.imageAnnotationsCollection = imageAnnotationsCollection;
        }

        public IterationStatus getStatus() {
            return status;
        }

        public void setStatus(IterationStatus status) {
            this.status = status;
        }
    }

    public static class AnnotationColorLabel {

        String name;
        String color;

        AnnotationColorLabel() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }

    public enum TaskCategory { SEGMENTATION, CLASSIFICATION }

    public enum IterationStatus { ANNOTATION, RUNNING, SUCCESSFUL, FAILED }
}
