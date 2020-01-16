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
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageHandler;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles.MetadataFileHandler;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 */
@Component
@RepositoryEventHandler
public class ImagesCollectionEventHandler {
	
	private static final Logger LOGGER = Logger.getLogger(ImagesCollectionEventHandler.class.getName());
	
    @Autowired
    CoreConfig config;

    @Autowired
    private ImagesCollectionRepository imagesCollectionRepository;

    @Autowired
    private ImageHandler imageRepository;

    @Autowired
    private MetadataFileHandler metadataFileRepository;

    @Autowired
    private ImagesCollectionLogic imagesCollectionLogic;

    @HandleBeforeCreate
    public void handleBeforeCreate(ImagesCollection imagesCollection) {
        imagesCollectionLogic.assertCollectionNameUnique(
                imagesCollection.getName());
        imagesCollection.setCreationDate(new Date());
        imagesCollection.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @HandleBeforeSave
    public void handleBeforeSave(ImagesCollection imagesCollection) {
    	Optional<ImagesCollection> result = imagesCollectionRepository.findById(
                imagesCollection.getId());
    	if (!result.isPresent()) {
        	throw new NotFoundException("Image collection with id " + imagesCollection.getId() + " not found");
        }
        boolean var1 = !imagesCollection.isPubliclyAvailable();
    	String var2 = imagesCollection.getOwner();
        String var3 = SecurityContextHolder.getContext().getAuthentication().getName();
    	if (var1 && !var2.equals(var3)){
            throw new ClientException("You do not have the right to edit this collection.");
        }

        ImagesCollection oldTc = result.get();


        if (!Objects.equals(
                imagesCollection.getCreationDate(),
                oldTc.getCreationDate())) {
            throw new ClientException("Can not change creation date.");
        }

        if (!Objects.equals(
                imagesCollection.getSourceJob(),
                oldTc.getSourceJob())) {
            throw new ClientException("Can not change source job.");
        }

        if (!Objects.equals(imagesCollection.getName(), oldTc.getName())) {
            imagesCollectionLogic.assertCollectionNameUnique(
                    imagesCollection.getName());
        }

        if (imagesCollection.isLocked() != oldTc.isLocked()) {
            if (!imagesCollection.isLocked()) {
                throw new ClientException("Can not unlock images collection.");
            }
            imagesCollectionLogic.assertCollectionNotImporting(oldTc);
            imagesCollectionLogic.assertCollectionHasNoImportError(oldTc);
        }
    }

    @HandleBeforeDelete
    public void handleBeforeDelete(ImagesCollection imagesCollection) {
    	Optional<ImagesCollection> result = imagesCollectionRepository.findById(
                imagesCollection.getId());
    	if (!result.isPresent()) {
        	throw new NotFoundException("Image collection with id " + imagesCollection.getId() + " not found");
        }

        ImagesCollection oldTc = result.get();
        imagesCollectionLogic.assertCollectionNotLocked(oldTc);
    }

    @HandleAfterDelete
    public void handleAfterDelete(ImagesCollection imagesCollection) {
    	imageRepository.deleteAll(imagesCollection.getId(), false);
    	metadataFileRepository.deleteAll(imagesCollection.getId(), false);
    	File imagesCollectionFolder = new File (config.getImagesCollectionsFolder(), imagesCollection.getId());
    	try {
    		FileUtils.deleteDirectory(imagesCollectionFolder);
    	} catch (IOException e) {
    		LOGGER.log(Level.WARNING, "Was not able to delete the image collection folder " + imagesCollectionFolder);
    	}	
    }

}
