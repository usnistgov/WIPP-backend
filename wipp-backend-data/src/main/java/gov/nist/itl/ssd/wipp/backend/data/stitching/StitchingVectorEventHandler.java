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
package gov.nist.itl.ssd.wipp.backend.data.stitching;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Component
@RepositoryEventHandler(StitchingVector.class)
public class StitchingVectorEventHandler {
	
	@Autowired
	StitchingVectorRepository stitchingVectorRepository;
	
	@Autowired
	StitchingVectorLogic stitchingVectorLogic;
	
	@Autowired
    CoreConfig config;

    @PreAuthorize("isAuthenticated()")
    @HandleBeforeCreate
    public void handleBeforeCreate(StitchingVector stitchingVector) {
    	throw new ClientException("Creation of Stitching Vector via REST API is not allowed.");
    }

    @HandleBeforeSave
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #stitchingVector.owner == principal.name)")
    public void handleBeforeSave(StitchingVector stitchingVector) {
    	// Assert data exists
        Optional<StitchingVector> result = stitchingVectorRepository.findById(
        		stitchingVector.getId());
        if (!result.isPresent()) {
            throw new NotFoundException("StitchingVector with id " + stitchingVector.getId() + " not found");
        }
        
        StitchingVector oldSv = result.get();
        
        // Source job cannot be changed
        if (!Objects.equals(
        		stitchingVector.getStitchingJob(),
        		oldSv.getStitchingJob())) {
            throw new ClientException("Can not change source job.");
        }

        // Assert data name is unique
        if (!Objects.equals(stitchingVector.getName(), oldSv.getName())) {
            stitchingVectorLogic.assertStitchingVectorNameUnique(
            		stitchingVector.getName());
        }
        
        // A public data cannot become private
        if (oldSv.isPubliclyShared() && !stitchingVector.isPubliclyShared()){
            throw new ClientException("Can not change a public data to private.");
        }
                
        // Owner cannot be changed
        if (!Objects.equals(
        		stitchingVector.getOwner(),
        		oldSv.getOwner())) {
            throw new ClientException("Can not change owner.");
        }
    }
}
