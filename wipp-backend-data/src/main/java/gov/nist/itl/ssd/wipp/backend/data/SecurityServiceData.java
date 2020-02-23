package gov.nist.itl.ssd.wipp.backend.data;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.CsvCollection;
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.CsvCollectionRepository;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollectionRepository;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.Image;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageRepository;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles.MetadataFile;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles.MetadataFileRepository;
import gov.nist.itl.ssd.wipp.backend.data.pyramid.Pyramid;
import gov.nist.itl.ssd.wipp.backend.data.pyramid.PyramidRepository;
import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVector;
import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVectorRepository;
import gov.nist.itl.ssd.wipp.backend.data.tensorboard.TensorboardLogs;
import gov.nist.itl.ssd.wipp.backend.data.tensorboard.TensorboardLogsRepository;
import gov.nist.itl.ssd.wipp.backend.data.tensorflowmodels.TensorflowModel;
import gov.nist.itl.ssd.wipp.backend.data.tensorflowmodels.TensorflowModelRepository;
import gov.nist.itl.ssd.wipp.backend.data.visualization.Visualization;
import gov.nist.itl.ssd.wipp.backend.data.visualization.VisualizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

/**
 * This class is responsible for doing the security checks on the secured objects.
 * If the object's id is provided, it will retrieve the object itself. If the object doesn't exist, it will throw a NotFoundException
 * If the object is provided, it will check that the object is publicly available or that the owner is the connected user if the object is private
 * If the user is not authorized, it will throw a ForbiddenException
 */

@Service
public class SecurityServiceData {
    @Autowired
    private ImagesCollectionRepository imagesCollectionRepository;
    @Autowired
    private CsvCollectionRepository csvCollectionRepository;
    @Autowired
    private MetadataFileRepository metadataFileRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private StitchingVectorRepository stitchingVectorRepository;
    @Autowired
    private VisualizationRepository visualizationRepository;
    @Autowired
    private TensorflowModelRepository tensorflowModelRepository;
    @Autowired
    private TensorboardLogsRepository tensorboardLogsRepository;
    @Autowired
    private PyramidRepository pyramidRepository;


    public boolean checkAuthorizeImageId(String imageId){
        Optional<Image> image = imageRepository.findById(imageId);
        if (image.isPresent()){
            return(checkAuthorize(image.get()));
        }
        else {
            throw new NotFoundException("Image with id " + imageId + " not found");
        }
    }
    public boolean checkAuthorize(Image image){
        return(checkAuthorizeImagesCollectionId(image.getImagesCollection()));
    }

    public boolean checkAuthorizeMetadataFileId(String metadatafileId){
        Optional<MetadataFile> metadataFile = metadataFileRepository.findById(metadatafileId);
        if (metadataFile.isPresent()){
            return(checkAuthorize(metadataFile.get()));
        }
        else {
            throw new NotFoundException("MetadataFile with id " + metadatafileId + " not found");
        }
    }
    public boolean checkAuthorize(MetadataFile metadataFile){
        return(checkAuthorizeImagesCollectionId(metadataFile.getImagesCollection()));
    }

    public boolean checkAuthorizeImagesCollectionId(String imagesCollectionId) {
        Optional<ImagesCollection> imagesCollection = imagesCollectionRepository.findById(imagesCollectionId);
        if (imagesCollection.isPresent()){
            return(checkAuthorize(imagesCollection.get()));
        }
        else {
            throw new NotFoundException("Image collection with id " + imagesCollectionId + " not found");
        }
    }


    public static boolean checkAuthorize(ImagesCollection imagesCollection) {
        String imagesCollectionOwner = imagesCollection.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!imagesCollection.isPubliclyAvailable() && imagesCollectionOwner != null && !imagesCollectionOwner.equals(connectedUser)) {
            throw new ForbiddenException("You do not have access to this image collection");
        }
        return(true);
    }


    public boolean checkAuthorizeCsvCollectionId(String csvCollectionId) {
        Optional<CsvCollection> csvCollection = csvCollectionRepository.findById(csvCollectionId);
        if (csvCollection.isPresent()){
            return(checkAuthorize(csvCollection.get()));
        }
        else {
            throw new NotFoundException("CSV collection with id " + csvCollectionId + " not found");
        }
    }

    public static boolean checkAuthorize(CsvCollection csvCollection) {
        String csvCollectionOwner = csvCollection.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!csvCollection.isPubliclyAvailable() && csvCollectionOwner != null && !csvCollectionOwner.equals(connectedUser)) {
            throw new ForbiddenException("You do not have access to this csv collection");
        }
        return(true);
    }

    public boolean checkAuthorizeStitchingVectorId(String stitchingVectorId) {
        Optional<StitchingVector> stitchingVector = stitchingVectorRepository.findById(stitchingVectorId);
        if (stitchingVector.isPresent()){
            return(checkAuthorize(stitchingVector.get()));
        }
        else {
            throw new NotFoundException("Stitching vector with id " + stitchingVectorId + " not found");
        }
    }

    public static boolean checkAuthorize(StitchingVector stitchingVector) {
        String stitchingVectorOwner = stitchingVector.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!stitchingVector.isPubliclyAvailable() && stitchingVectorOwner != null && !stitchingVectorOwner.equals(connectedUser)) {
            throw new ForbiddenException("You do not have access to this stitching vector");
        }
        return(true);
    }

    public boolean checkAuthorizeVisualizationId(String visualizationId) {
        Optional<Visualization> visualization = visualizationRepository.findById(visualizationId);
        if (visualization.isPresent()){
            return(checkAuthorize(visualization.get()));
        }
        else {
            throw new NotFoundException("Visualization with id " + visualizationId + " not found");
        }
    }

    public static boolean checkAuthorize(Visualization visualization) {
        String visualizationOwner = visualization.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!visualization.isPubliclyAvailable() && visualizationOwner != null && !visualizationOwner.equals(connectedUser)) {
            throw new ForbiddenException("You do not have access to this visualization");
        }
        return(true);
    }

    public boolean checkAuthorizeTensorflowModelId(String tensorflowModelId) {
        Optional<TensorflowModel> tensorflowModel = tensorflowModelRepository.findById(tensorflowModelId);
        if (tensorflowModel.isPresent()){
            return(checkAuthorize(tensorflowModel.get()));
        }
        else {
            throw new NotFoundException("Tensorflow Model with id " + tensorflowModelId + " not found");
        }
    }

    public static boolean checkAuthorize(TensorflowModel tensorflowModel) {
        String tensorflowModelOwner = tensorflowModel.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!tensorflowModel.isPubliclyAvailable() && tensorflowModelOwner != null && !tensorflowModelOwner.equals(connectedUser)) {
            throw new ForbiddenException("You do not have access to this Tensorflow Model");
        }
        return(true);
    }

    public boolean checkAuthorizeTensorboardLogsId(String tensorboardLogsId) {
        Optional<TensorboardLogs> tensorboardLogs = tensorboardLogsRepository.findById(tensorboardLogsId);
        if (tensorboardLogs.isPresent()){
            return(checkAuthorize(tensorboardLogs.get()));
        }
        else {
            throw new NotFoundException("Tensorboard logs with id " + tensorboardLogsId + " not found");
        }
    }

    public static boolean checkAuthorize(TensorboardLogs tensorboardLogs) {
        String tensorboardLogsOwner = tensorboardLogs.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!tensorboardLogs.isPubliclyAvailable() && tensorboardLogsOwner != null && !tensorboardLogsOwner.equals(connectedUser)) {
            throw new ForbiddenException("You do not have access to this Tensorboard logs");
        }
        return(true);
    }

    public boolean checkAuthorizePyramidId(String pyramidId) {
        Optional<Pyramid> pyramid = pyramidRepository.findById(pyramidId);
        if (pyramid.isPresent()){
            return(checkAuthorize(pyramid.get()));
        }
        else {
            throw new NotFoundException("Pyramid with id " + pyramidId + " not found");
        }
    }

    public static boolean checkAuthorize(Pyramid pyramid) {
        String pyramidOwner = pyramid.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!pyramid.isPubliclyAvailable() && pyramidOwner != null && !pyramidOwner.equals(connectedUser)) {
            throw new ForbiddenException("You do not have access to this Pyramid");
        }
        return(true);
    }

    /**
     * This method is needed to make sure the user is logged in. This is a workaround, because Keycloak's hasRole() method is not working.
     */

    public static boolean hasUserRole(){
        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        for (GrantedAuthority grantedAuthority : authorities) {
            if(grantedAuthority.getAuthority().toString().equals("user")){
                return(true);
            }
        }
        return(false);
    }
}