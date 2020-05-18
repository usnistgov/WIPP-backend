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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

/**
 * Visualization Security service
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Service
public class VisualizationSecurity {
	
	@Autowired
    private VisualizationRepository visualizationRepository;

    public boolean checkAuthorize(String visualizationId, Boolean editMode) {
        Optional<Visualization> visualization = visualizationRepository.findById(visualizationId);
        if (visualization.isPresent()){
            return(checkAuthorize(visualization.get(), editMode));
        }
        else {
            throw new NotFoundException("Visualization with id " + visualizationId + " not found");
        }
    }

    public static boolean checkAuthorize(Visualization visualization, Boolean editMode) {
        String visualizationOwner = visualization.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!visualization.isPubliclyShared() && (visualizationOwner == null || !visualizationOwner.equals(connectedUser))) {
            throw new ForbiddenException("You do not have access to this Visualization");
        }
        if (visualization.isPubliclyShared() && editMode && (visualizationOwner == null || !visualizationOwner.equals(connectedUser))){
            throw new ForbiddenException("You do not have the right to edit this Visualization");
        }
        return(true);
    }

}
