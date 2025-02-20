package gov.nist.itl.ssd.wipp.backend.data.imageannotations;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.events.AllImagesDoneConvertingEvent;
import gov.nist.itl.ssd.wipp.backend.core.utils.SecurityUtils;
import gov.nist.itl.ssd.wipp.backend.data.imageannotations.annotations.ImageAnnotation;
import gov.nist.itl.ssd.wipp.backend.data.imageannotations.annotations.ImageAnnotationHandler;
import gov.nist.itl.ssd.wipp.backend.data.imageannotations.annotations.ImageAnnotationRepository;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollectionRepository;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.Image;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageConversionService;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageHandler;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.util.List;

/**
 * Image Annotations webhook receiver controller
 *
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@RestController
@Tag(name="ImageAnnotations Entity")
@RequestMapping(CoreConfig.BASE_URI + "/imageAnnotationsCollections/webhookReceiver/{taskId}")
public class ImageAnnotationsTaskWebhookReceiver {

    @Autowired
    CoreConfig config;

    @Autowired
    ImageAnnotationsCollectionRepository imageAnnotationsCollectionRepository;

    @Autowired
    ImageAnnotationRepository imageAnnotationRepository;

    @Autowired
    ImageAnnotationHandler imageAnnotationHandler;

    @Autowired
    ImagesCollectionRepository imagesCollectionRepository;

    @Autowired
    private ImageHandler imageHandler;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageConversionService imageConversionService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @RequestMapping(
            value = "",
            method = RequestMethod.GET,
            produces = "application/json")
//    @PreAuthorize("hasRole('admin') or @imageAnnotationsCollectionSecurity.checkAuthorize(#imageAnnotationsCollectionId, false)")
    public void notify(
            @PathVariable("taskId") String taskId) {

        // Load security context for system operations
        SecurityUtils.runAsSystem();

        // Check existence of image annotations collection
        ImageAnnotationsCollection imageAnnotationsCollection = imageAnnotationsCollectionRepository.findByTaskId(
                taskId);
        if (imageAnnotationsCollection == null) {
            throw new ResourceNotFoundException(
                    "Image annotations collection for task " + taskId + " not found.");
        }

        // Request download
        RestClient restClient = RestClient.create();
        AnnotationsDownloadRequestBody annotationsDownloadRequestBody = new AnnotationsDownloadRequestBody();
        annotationsDownloadRequestBody.setTask_id(Integer.parseInt(taskId));
        annotationsDownloadRequestBody.setFormat(new String[]{"datumaro", "svg", "mask"});
        annotationsDownloadRequestBody.setAnnotation_dir("/app/upload_data/" + imageAnnotationsCollection.getId());
        annotationsDownloadRequestBody.setMask_dir("/app/upload_data/" + imageAnnotationsCollection.getId() + "/masks");
        annotationsDownloadRequestBody.setMask_type(1);
        ResponseEntity<Void> response = restClient.post()
                .uri(config.getAnnotApiUrl() + "/download")
                .contentType(MediaType.APPLICATION_JSON)
                .body(annotationsDownloadRequestBody)
                .retrieve()
                .toBodilessEntity();

        // Create/get mask collection
        boolean isPartOfIterativeAiPipeline = false;
        ImagesCollection maskCollection = null;
        if (imageAnnotationsCollection.getTargetMaskCollectionId() != null ) {
            isPartOfIterativeAiPipeline = true;
            maskCollection = imagesCollectionRepository.findById(imageAnnotationsCollection.getTargetMaskCollectionId()).orElse(null);
        }
        if (maskCollection == null) {
            maskCollection = new ImagesCollection(imageAnnotationsCollection.getName() + "-masks",
                    true, ImagesCollection.ImagesCollectionImportMethod.ANNOT, ImagesCollection.ImagesCollectionFormat.OMETIFF);
            maskCollection.setOwner(imageAnnotationsCollection.getOwner());
            maskCollection.setSourceAnnotationCollection(imageAnnotationsCollection.getId());
            maskCollection = imagesCollectionRepository.save(maskCollection);
            // TODO: set as annot target mask coll?
        }
        String maskCollectionId = maskCollection.getId();
        File imagesCollectionTempFolder = new File(new File(config.getImageAnnotationsFolder(), imageAnnotationsCollection.getId()), "masks");

        // Register annotations and masks
        List<ImageAnnotation> annotationList = imageAnnotationRepository.findAll();
        annotationList.forEach(imageAnnotation -> {
            boolean annotationFound = false;
            String datumaroFileName = imageAnnotation.getImageFileName() + ".json";
            String annotoriousFileName = imageAnnotation.getImageFileName() + ".svg.json";
            // Check Datumaro file
            if(imageAnnotationHandler.getFile(imageAnnotation.getImageAnnotationsCollection(), datumaroFileName).exists()) {
                imageAnnotation.setDatumaroFileName(datumaroFileName);
                annotationFound = true;
            };
            // Check Annotorious/SVG file
            if(imageAnnotationHandler.getFile(imageAnnotation.getImageAnnotationsCollection(), annotoriousFileName).exists()) {
                imageAnnotation.setAnnotoriousFileName(annotoriousFileName);
                annotationFound = true;
            };
            // Check mask image
            if(new File(imagesCollectionTempFolder, imageAnnotation.getImageFileName()).exists()) {
                imageAnnotation.setImageMask(
                        new ImageAnnotation.ImageAnnotationMask(maskCollectionId, imageAnnotation.getImageFileName())
                );
                annotationFound = true;
            }
            // Update annotation status in database if needed
            if(annotationFound) {
                imageAnnotation.setPending(false);
                imageAnnotationRepository.save(imageAnnotation);
            }
        });

        // Import and convert images
        List<Image> images = imageHandler.addAllInDbFromFolder(maskCollectionId, imagesCollectionTempFolder.getPath());
        for(Image image : images) {
            imageConversionService.submitImageToExtractor(image, imagesCollectionTempFolder, true);
        }

        // if no images need to be converted, send event to start workflow
        if(isPartOfIterativeAiPipeline && images.isEmpty()) {
            eventPublisher.publishEvent(new AllImagesDoneConvertingEvent(maskCollectionId));
        }

        // Clear security context after system operations
        SecurityContextHolder.clearContext();
    }

    private static class AnnotationsDownloadRequestBody {

        @JsonProperty("task_id")
        private int task_id;
        private String[] format;
        private String annotation_dir;
        private String mask_dir;
        private int mask_type;

        public AnnotationsDownloadRequestBody() {
        }

        public int getTask_id() {
            return task_id;
        }

        public void setTask_id(int task_id) {
            this.task_id = task_id;
        }

        public String[] getFormat() {
            return format;
        }

        public void setFormat(String[] format) {
            this.format = format;
        }

        public String getAnnotation_dir() {
            return annotation_dir;
        }

        public void setAnnotation_dir(String annotation_dir) {
            this.annotation_dir = annotation_dir;
        }

        public String getMask_dir() {
            return mask_dir;
        }

        public void setMask_dir(String mask_dir) {
            this.mask_dir = mask_dir;
        }

        public int getMask_type() { return mask_type; }

        public void setMask_type(int mask_type) { this.mask_type = mask_type; }
    }
}
