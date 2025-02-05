package gov.nist.itl.ssd.wipp.backend.data.imageannotations.annotations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualRef;
import gov.nist.itl.ssd.wipp.backend.data.imageannotations.ImageAnnotationsCollection;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@CompoundIndexes({
        @CompoundIndex(
                name = "collection_filename",
                def = "{'imageAnnotationsCollection': 1, 'imageFileName': 1}",
                unique = true)
})
public class ImageAnnotation {

    @Id
    @JsonIgnore
    private String id;

    @Indexed
    @ManualRef(ImageAnnotationsCollection.class)
    private String imageAnnotationsCollection;

    private String imageFileName;

    private boolean pending;

    private String taskId;

    private String datumaroFileName;

    private String annotoriousFileName;

    private ImageAnnotationMask imageMask;

    public ImageAnnotation() {
    }

    public String getId() {
        return id;
    }

    public String getImageAnnotationsCollection() {
        return imageAnnotationsCollection;
    }

    public void setImageAnnotationsCollection(String imageAnnotationsCollection) {
        this.imageAnnotationsCollection = imageAnnotationsCollection;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getDatumaroFileName() {
        return datumaroFileName;
    }

    public void setDatumaroFileName(String datumaroFileName) {
        this.datumaroFileName = datumaroFileName;
    }

    public String getAnnotoriousFileName() {
        return annotoriousFileName;
    }

    public void setAnnotoriousFileName(String annotoriousFileName) {
        this.annotoriousFileName = annotoriousFileName;
    }

    public ImageAnnotationMask getImageMask() {
        return imageMask;
    }

    public void setImageMask(ImageAnnotationMask imageMask) {
        this.imageMask = imageMask;
    }

    public static class ImageAnnotationMask {

        private String imagesCollectionId;
        private String imageFileName;

        public ImageAnnotationMask() {
        }

        public ImageAnnotationMask(String imagesCollectionId, String imageFileName) {
            this.imagesCollectionId = imagesCollectionId;
            this.imageFileName = imageFileName;
        }

        public String getImagesCollectionId() {
            return imagesCollectionId;
        }

        public void setImagesCollectionId(String imagesCollectionId) {
            this.imagesCollectionId = imagesCollectionId;
        }

        public String getImageFileName() {
            return imageFileName;
        }

        public void setImageFileName(String imageFileName) {
            this.imageFileName = imageFileName;
        }
    }

}
