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
package gov.nist.itl.ssd.wipp.backend.data.visualization;

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

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Component
@RepositoryEventHandler(Visualization.class)
public class VisualizationEventHandler {
	
	@Autowired
	VisualizationRepository visualizationRepository;

	@PreAuthorize("isAuthenticated()")
    @HandleBeforeCreate
    public void handleBeforeCreate(Visualization visualization) {
		// Assert visualization name is unique
        visualization.setCreationDate(new Date());
        
        // Set the owner to the connected user
        visualization.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());
    }
	
	@HandleBeforeSave
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #visualization.owner == principal.name)")
    public void handleBeforeSave(Visualization visualization) {
    	// Assert visualization exists
        Optional<Visualization> result = visualizationRepository.findById(
        		visualization.getId());
    	if (!result.isPresent()) {
        	throw new NotFoundException("Visualization with id " + visualization.getId() + " not found");
        }

    	Visualization oldViz = result.get();

    	// A public visualization cannot become private
    	if (oldViz.isPubliclyShared() && !visualization.isPubliclyShared()){
            throw new ClientException("Can not set change a public visualization to private.");
        }
    	
    	// Owner cannot be changed
        if (!Objects.equals(
        		visualization.getOwner(),
                oldViz.getOwner())) {
            throw new ClientException("Can not change owner.");
        }

    	// Creation date cannot be changed
        if (!Objects.equals(
        		visualization.getCreationDate(),
                oldViz.getCreationDate())) {
            throw new ClientException("Can not change creation date.");
        }
    }


}
