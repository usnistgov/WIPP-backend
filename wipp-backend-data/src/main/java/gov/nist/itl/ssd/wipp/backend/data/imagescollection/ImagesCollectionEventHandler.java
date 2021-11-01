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
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection.ImagesCollectionImportMethod;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageHandler;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles.MetadataFileHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;
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
 * @author Mylene Simon <mylene.simon at nist.gov>
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

    @PreAuthorize("isAuthenticated()")
    @HandleBeforeCreate
    public void handleBeforeCreate(ImagesCollection imagesCollection) {
    	// Assert imagesCollection name is unique
        imagesCollectionLogic.assertCollectionNameUnique(
                imagesCollection.getName());
        
        // Set creation date to current date
        imagesCollection.setCreationDate(new Date());

        // Set the owner to the connected user
        imagesCollection.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());

        
        // Default import method is UPLOADED
        if (imagesCollection.getImportMethod() == null) {
        	imagesCollection.setImportMethod(ImagesCollectionImportMethod.UPLOADED);
        }
        
        // Collections from Catalog are locked by default
        if (imagesCollection.getImportMethod() != null
        	   && imagesCollection.getImportMethod().equals(ImagesCollectionImportMethod.CATALOG)) {
        	imagesCollection.setLocked(true);
        }
    }

    @HandleBeforeSave
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #imagesCollection.owner == principal.name)")
    public void handleBeforeSave(ImagesCollection imagesCollection) {
    	// Assert collection exists
        Optional<ImagesCollection> result = imagesCollectionRepository.findById(
                imagesCollection.getId());
    	if (!result.isPresent()) {
        	throw new NotFoundException("Image collection with id " + imagesCollection.getId() + " not found");
        }

        ImagesCollection oldTc = result.get();

    	// A public collection cannot become private
    	if (oldTc.isPubliclyShared() && !imagesCollection.isPubliclyShared()){
            throw new ClientException("Can not set a public collection to private.");
        }
    	
    	// An unlocked collection cannot become public
    	if (!oldTc.isPubliclyShared() && imagesCollection.isPubliclyShared() && !oldTc.isLocked()){
            throw new ClientException("Can not set an unlocked collection to public, please lock collection first.");
        }
    	
    	// Owner cannot be changed
        if (!Objects.equals(
        		imagesCollection.getOwner(),
                oldTc.getOwner())) {
            throw new ClientException("Can not change owner.");
        }

    	// Creation date cannot be changed
        if (!Objects.equals(
                imagesCollection.getCreationDate(),
                oldTc.getCreationDate())) {
            throw new ClientException("Can not change creation date.");
        }

        // Import method cannot be changed
        if (!Objects.equals(
                imagesCollection.getImportMethod(),
                oldTc.getImportMethod())) {
            throw new ClientException("Can not change import method.");
        }
        
        // Source catalog cannot be changed
        if (!Objects.equals(
                imagesCollection.getSourceCatalog(),
                oldTc.getSourceCatalog())) {
            throw new ClientException("Can not change source catalog.");
        }
        
        // Source job cannot be changed
        if (!Objects.equals(
                imagesCollection.getSourceJob(),
                oldTc.getSourceJob())) {
            throw new ClientException("Can not change source job.");
        }

        // Assert collection name is unique
        if (!Objects.equals(imagesCollection.getName(), oldTc.getName())) {
            imagesCollectionLogic.assertCollectionNameUnique(
                    imagesCollection.getName());
        }

        // Cannot unlock locked collection
        if (imagesCollection.isLocked() != oldTc.isLocked()) {
            if (!imagesCollection.isLocked()) {
                throw new ClientException("Can not unlock images collection.");
            }
            imagesCollectionLogic.assertCollectionNotImporting(oldTc);
            imagesCollectionLogic.assertCollectionHasNoImportError(oldTc);
        }
    }

    @HandleBeforeDelete
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or "
    		+ "(#imagesCollection.owner == principal.name and #imagesCollection.publiclyShared == false))")
    public void handleBeforeDelete(ImagesCollection imagesCollection) {
    	// Assert collection exists
    	Optional<ImagesCollection> result = imagesCollectionRepository.findById(
                imagesCollection.getId());
    	if (!result.isPresent()) {
        	throw new NotFoundException("Image collection with id " + imagesCollection.getId() + " not found");
        }
    }

    @HandleAfterDelete
    public void handleAfterDelete(ImagesCollection imagesCollection) {
    	// Delete all images and metadataFiles from deleted collection
    	imageRepository.deleteAll(imagesCollection.getId(), false);
    	metadataFileRepository.deleteAll(imagesCollection.getId(), false);
    	File imagesCollectionFolder = new File (config.getImagesCollectionsFolder(), imagesCollection.getId());
    	try {
    		FileUtils.deleteDirectory(imagesCollectionFolder);
    	} catch (IOException e) {
    		LOGGER.log(Level.WARNING, 
    				"Was not able to delete the image collection folder " + imagesCollectionFolder);
    	}	
    	// Delete temporary upload folder if any
    	File imagesCollectionTempFolder = new File (config.getCollectionsUploadTmpFolder(), imagesCollection.getId());
    	try {
    		if (imagesCollectionTempFolder.exists()) {
    			FileUtils.deleteDirectory(imagesCollectionTempFolder);
    		}
    	} catch (IOException e) {
    		LOGGER.log(Level.WARNING, 
    				"Was not able to delete the image collection upload folder " + imagesCollectionTempFolder);
    	}	
    }

}
