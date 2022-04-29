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
package gov.nist.itl.ssd.wipp.backend.data.genericdatacollection;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.genericdatacollection.genericfiles.GenericFileHandler;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 * @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
 *
 */
@Component
@RepositoryEventHandler(GenericDataCollection.class)
public class GenericDataCollectionEventHandler {

	private static final Logger LOGGER = Logger.getLogger(GenericDataCollectionEventHandler.class.getName());
	
    @Autowired
    CoreConfig config;
    
    @Autowired
    private GenericDataCollectionRepository genericDataCollectionRepository;
    
    @Autowired
    private GenericFileHandler genericFileRepository;
    
    @Autowired
    private GenericDataCollectionLogic genericDataCollectionLogic;



    @PreAuthorize("isAuthenticated()")
    @HandleBeforeCreate
    public void handleBeforeCreate(GenericDataCollection genericDataCollection) {
    	
    	// Assert collection name is unique
    	genericDataCollectionLogic.assertCollectionNameUnique(
    			genericDataCollection.getName());
    	
    	// Set creation date to current date
    	genericDataCollection.setCreationDate(new Date());
        
        // Set the owner to the connected user
    	genericDataCollection.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());

    }

    @HandleBeforeSave
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #genericDataCollection.owner == principal.name)")
    public void handleBeforeSave(GenericDataCollection genericDataCollection) {
    	// Assert collection exists
        Optional<GenericDataCollection> result = genericDataCollectionRepository.findById(
        		genericDataCollection.getId());
        if (!result.isPresent()) {
            throw new NotFoundException("Generic Data collection with id " + genericDataCollection.getId() + " not found");
        }
        
        GenericDataCollection oldGd = result.get();
        
        // A public data cannot become private
        if (oldGd.isPubliclyShared() && !genericDataCollection.isPubliclyShared()){
            throw new ClientException("Can not change a public data to private.");
        }
                
        // Owner cannot be changed
        if (!Objects.equals(
        		genericDataCollection.getOwner(),
        		oldGd.getOwner())) {
            throw new ClientException("Can not change owner.");
        }
        
        // Cannot unlock locked collection
        if (genericDataCollection.isLocked() != oldGd.isLocked()) {
            if (!genericDataCollection.isLocked()) {
                throw new ClientException("Can not unlock Generic Data collection.");
            }
            genericDataCollectionLogic.assertCollectionNotImporting(oldGd);
            genericDataCollectionLogic.assertCollectionHasNoImportError(oldGd);
        }
    }

    @HandleBeforeDelete
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #genericDataCollection.owner == principal.name)")
    public void handleBeforeDelete(GenericDataCollection genericDataCollection) {
    	// Assert collection exists
    	Optional<GenericDataCollection> result = genericDataCollectionRepository.findById(
    			genericDataCollection.getId());
    	if (!result.isPresent()) {
        	throw new NotFoundException("Generic Data collection with id " + genericDataCollection.getId() + " not found");
        }

    	GenericDataCollection oldGd = result.get();
        
        // Locked collection cannot be deleted
    	genericDataCollectionLogic.assertCollectionNotLocked(oldGd);
    }

    @HandleAfterDelete
    public void handleAfterDelete(GenericDataCollection genericDataCollection) {
    	// Delete all Generic files from deleted collection
    	genericFileRepository.deleteAll(genericDataCollection.getId());
    	File genericDataCollectionFolder = new File (config.getGenericDataCollectionsFolder(), genericDataCollection.getId());
    	try {
    		FileUtils.deleteDirectory(genericDataCollectionFolder);
    	} catch (IOException e) {
    		LOGGER.log(Level.WARNING, "Was not able to delete the Generic Data collection folder " + genericDataCollectionFolder);
    	}	
    }
}
