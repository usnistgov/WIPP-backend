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
package gov.nist.itl.ssd.wipp.backend.data.genericdata;

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
import gov.nist.itl.ssd.wipp.backend.data.genericdata.genericfiles.GenericFileHandler;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 * @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
 *
 */
@Component
@RepositoryEventHandler(GenericData.class)
public class GenericDataEventHandler {

	private static final Logger LOGGER = Logger.getLogger(GenericDataEventHandler.class.getName());
	
    @Autowired
    CoreConfig config;
    
    @Autowired
    private GenericDataRepository genericDataRepository;
    
    @Autowired
    private GenericFileHandler genericFileRepository;
    
    @Autowired
    private GenericDataLogic genericDataLogic;



    @PreAuthorize("isAuthenticated()")
    @HandleBeforeCreate
    public void handleBeforeCreate(GenericData genericData) {
    	
    	// Assert collection name is unique
    	genericDataLogic.assertCollectionNameUnique(
    			genericData.getName());
    	
    	// Set creation date to current date
    	genericData.setCreationDate(new Date());
        
        // Set the owner to the connected user
    	genericData.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());

    	throw new ClientException("Creation of Generic Data via REST API is not allowed.");
    }

    @HandleBeforeSave
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #genericData.owner == principal.name)")
    public void handleBeforeSave(GenericData genericData) {
    	// Assert collection exists
        Optional<GenericData> result = genericDataRepository.findById(
        		genericData.getId());
        if (!result.isPresent()) {
            throw new NotFoundException("Generic Data with id " + genericData.getId() + " not found");
        }
        
        GenericData oldGd = result.get();
        
        // A public data cannot become private
        if (oldGd.isPubliclyShared() && !genericData.isPubliclyShared()){
            throw new ClientException("Can not change a public data to private.");
        }
                
        // Owner cannot be changed
        if (!Objects.equals(
        		genericData.getOwner(),
        		oldGd.getOwner())) {
            throw new ClientException("Can not change owner.");
        }
        
        // Cannot unlock locked collection
        if (genericData.isLocked() != oldGd.isLocked()) {
            if (!genericData.isLocked()) {
                throw new ClientException("Can not unlock Generic Data collection.");
            }
            genericDataLogic.assertCollectionNotImporting(oldGd);
            genericDataLogic.assertCollectionHasNoImportError(oldGd);
        }
    }

    @HandleBeforeDelete
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #genericData.owner == principal.name)")
    public void handleBeforeDelete(GenericData genericData) {
    	// Assert collection exists
    	Optional<GenericData> result = genericDataRepository.findById(
    			genericData.getId());
    	if (!result.isPresent()) {
        	throw new NotFoundException("Generic Data collection with id " + genericData.getId() + " not found");
        }

    	GenericData oldGd = result.get();
        
        // Locked collection cannot be deleted
        genericDataLogic.assertCollectionNotLocked(oldGd);
    }

    @HandleAfterDelete
    public void handleAfterDelete(GenericData genericData) {
    	// Delete all Generic files from deleted collection
    	genericFileRepository.deleteAll(genericData.getId());
    	File genericDataCollectionFolder = new File (config.getCsvCollectionsFolder(), genericData.getId());
    	try {
    		FileUtils.deleteDirectory(genericDataCollectionFolder);
    	} catch (IOException e) {
    		LOGGER.log(Level.WARNING, "Was not able to delete the Generic Data collection folder " + genericDataCollectionFolder);
    	}	
    }
}
