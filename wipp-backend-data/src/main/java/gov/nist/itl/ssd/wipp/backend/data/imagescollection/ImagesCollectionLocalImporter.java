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

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.Image;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageConversionService;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageHandler;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageRepository;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles.MetadataFileHandler;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Service class for local backend import
 @author Mylene Simon <mylene.simon at nist.gov>
 **/
@Service
public class ImagesCollectionLocalImporter {

    @Autowired
    CoreConfig config;

    @Autowired
    private ImagesCollectionRepository imagesCollectionRepository;

    @Autowired
    private ImageHandler imageHandler;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private MetadataFileHandler metadataHandler;

    @Autowired
    private ImageConversionService imageConversionService;

    protected void importFromLocalFolder(ImagesCollection imagesCollection) {
        try {
            File localImportFolder = new File(config.getLocalImportFolder(), imagesCollection.getSourceBackendImport());

            // Register images in collection
            imageHandler.addAllInDbFromFolder(imagesCollection.getId(), localImportFolder.getPath());
            List<Image> images = imageRepository.findByImagesCollection(imagesCollection.getId());

            // Start conversion (without source image deletion)
            for(Image image : images) {
                imageConversionService.submitImageToExtractor(image, localImportFolder, false);
            }

            // Import metadata files
            File metadataFolder = new File(localImportFolder, "metadata_files");
            if(metadataFolder.exists()) {
                metadataHandler.importFolderCopy(imagesCollection.getId(), metadataFolder);
            }

        } catch (IOException ex) {
            throw new ClientException("Error while importing data.");
        }
    }

    protected void importFromNexusLims(String activityName, List<String> activityImageLocations, String dataPid) {
        try {
            System.out.println("ImportFromNexusLims");
            File nexusLimsFolder = new File(config.getNexusLimsImportFolder());
            System.out.println(nexusLimsFolder);
            System.out.println(activityImageLocations);

            // Create images collection for the activity
            ImagesCollection activityCollection = new ImagesCollection(activityName, true,
                        ImagesCollection.ImagesCollectionImportMethod.NEXUSLIMS, ImagesCollection.ImagesCollectionFormat.OMETIFF);
            activityCollection.setDataPid(dataPid);
            activityCollection.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());
            activityCollection.setNotes("Images imported from NexusLims, PID " + dataPid + ".");
            imagesCollectionRepository.save(activityCollection);

            // Register images in collection
            List<Image> images = imageHandler.addAllInDbFromFolderAndRelativePaths(activityCollection.getId(), 
                        nexusLimsFolder.getPath(), activityImageLocations);

            // Start conversion (without source image deletion)
            for(Image image : images) {
                System.out.println("Start conversion of image " + image.getFileName() + " from location " + image.getOriginalFilePath());
                imageConversionService.submitImageToExtractor(image, nexusLimsFolder, false);
            }

            // Import metadata files
            for(String imageLocation : activityImageLocations) {
                File imageMetadataFile = new File(nexusLimsFolder, imageLocation + ".json");
                if(imageMetadataFile.exists()) {
                    System.out.println("Importing metadata file " + imageMetadataFile.getPath());
                    metadataHandler.importFileCopy(activityCollection.getId(), imageMetadataFile);
                }
            }

        } catch (IOException ex) {
            throw new ClientException("Error while importing data.");
        }
    }
}
