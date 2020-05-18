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
package gov.nist.itl.ssd.wipp.backend.data.csvCollection;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.csv.CsvHandler;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Samia Benjida <samia.benjida at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Component
@RepositoryEventHandler(CsvCollection.class)
public class CsvCollectionEventHandler {

    private static final Logger LOGGER = Logger.getLogger(CsvCollectionEventHandler.class.getName());

    @Autowired
    private CsvCollectionRepository csvCollectionRepository;
    
    @Autowired
    private CsvHandler csvRepository;
    
    @Autowired
    private CsvCollectionLogic csvCollectionLogic;

    @Autowired
    CoreConfig config;

    @PreAuthorize("isAuthenticated()")
    @HandleBeforeCreate
    public void handleBeforeCreate(CsvCollection csvCollection) {
    	// Assert collection name is unique
    	csvCollectionLogic.assertCollectionNameUnique(
    			csvCollection.getName());
    	
    	// Set creation date to current date
        csvCollection.setCreationDate(new Date());
        
        // Set the owner to the connected user
        csvCollection.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());

    }

    @HandleBeforeSave
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #csvCollection.owner == principal.name)")
    public void handleBeforeSave(CsvCollection csvCollection) {
    	// Assert collection exists
        Optional<CsvCollection> result = csvCollectionRepository.findById(
                csvCollection.getId());
        if (!result.isPresent()) {
            throw new NotFoundException("CSV collection with id " + csvCollection.getId() + " not found");
        }
        
        CsvCollection oldTc = result.get();
        
        // A public collection cannot become private
        if (oldTc.isPubliclyShared() && !csvCollection.isPubliclyShared()){
            throw new ClientException("Can not set change a public collection to private.");
        }
        
        // Owner cannot be changed
        if (!Objects.equals(
        		csvCollection.getOwner(),
                oldTc.getOwner())) {
            throw new ClientException("Can not change owner.");
        }
        
        // Cannot unlock locked collection
        if (csvCollection.isLocked() != oldTc.isLocked()) {
            if (!csvCollection.isLocked()) {
                throw new ClientException("Can not unlock CSV collection.");
            }
            csvCollectionLogic.assertCollectionNotImporting(oldTc);
            csvCollectionLogic.assertCollectionHasNoImportError(oldTc);
        }
    }
    
    @HandleBeforeDelete
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #csvCollection.owner == principal.name)")
    public void handleBeforeDelete(CsvCollection csvCollection) {
    	// Assert collection exists
    	Optional<CsvCollection> result = csvCollectionRepository.findById(
    			csvCollection.getId());
    	if (!result.isPresent()) {
        	throw new NotFoundException("CSV collection with id " + csvCollection.getId() + " not found");
        }

    	CsvCollection oldTc = result.get();
        
        // Locked collection cannot be deleted
        csvCollectionLogic.assertCollectionNotLocked(oldTc);
    }

    @HandleAfterDelete
    public void handleAfterDelete(CsvCollection csvCollection) {
    	// Delete all CSV files from deleted collection
    	csvRepository.deleteAll(csvCollection.getId());
    	File csvCollectionFolder = new File (config.getCsvCollectionsFolder(), csvCollection.getId());
    	try {
    		FileUtils.deleteDirectory(csvCollectionFolder);
    	} catch (IOException e) {
    		LOGGER.log(Level.WARNING, "Was not able to delete the CSV collection folder " + csvCollectionFolder);
    	}	
    }
}

