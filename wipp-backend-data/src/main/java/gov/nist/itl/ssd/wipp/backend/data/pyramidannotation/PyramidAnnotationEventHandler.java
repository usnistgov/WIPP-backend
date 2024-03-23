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
package gov.nist.itl.ssd.wipp.backend.data.pyramidannotation;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Component
@RepositoryEventHandler(PyramidAnnotation.class)
public class PyramidAnnotationEventHandler {

	@Autowired
	PyramidAnnotationRepository pyramidAnnotationRepository;
	
	@Autowired
    CoreConfig config;

    @PreAuthorize("isAuthenticated() and (hasRole('admin') or @pyramidSecurity.checkAuthorize(#pyramidAnnotation.pyramid, true))")
    @HandleBeforeCreate
    public void handleBeforeCreate(PyramidAnnotation pyramidAnnotation) {
    	// Set creation date to current date
    	pyramidAnnotation.setCreationDate(new Date());
        
        // Set the owner to the connected user
    	pyramidAnnotation.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @HandleBeforeSave
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #pyramidAnnotation.owner == authentication.name)")
    public void handleBeforeSave(PyramidAnnotation pyramidAnnotation) {
    	// Assert annotation exists
        Optional<PyramidAnnotation> result = pyramidAnnotationRepository.findById(
        		pyramidAnnotation.getId());
        if (!result.isPresent()) {
            throw new NotFoundException("Pyramid Annotation with id " + pyramidAnnotation.getId() + " not found");
        }
        
        PyramidAnnotation oldPyrAnnot = result.get();
        
        // A public data cannot become private
        if (oldPyrAnnot.isPubliclyShared() && !pyramidAnnotation.isPubliclyShared()){
            throw new ClientException("Can not change a public data to private.");
        }
                
        // Owner cannot be changed
        if (!Objects.equals(
        		pyramidAnnotation.getOwner(),
        		oldPyrAnnot.getOwner())) {
            throw new ClientException("Can not change owner.");
        }
    }
}
