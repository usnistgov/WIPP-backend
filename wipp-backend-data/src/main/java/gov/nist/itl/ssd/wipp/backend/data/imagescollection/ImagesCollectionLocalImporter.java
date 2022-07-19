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

import java.io.File;
import java.io.IOException;
import java.util.List;

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

            // Copy images to collection temp folder and start conversion
            for(Image image : images) {
                FileUtils.copyFileToDirectory(new File(localImportFolder, image.getFileName()),
                        imageHandler.getTempFilesFolder(imagesCollection.getId()));
                imageConversionService.submitImageToExtractor(image);
            }

            // Import metadata files
            File metadataFolder = new File(localImportFolder, "metadata_files");
            if(metadataFolder.exists()) {
                metadataHandler.importFolder(imagesCollection.getId(), metadataFolder);
            }

        } catch (IOException ex) {
            throw new ClientException("Error while importing data.");
        }
    }
}
