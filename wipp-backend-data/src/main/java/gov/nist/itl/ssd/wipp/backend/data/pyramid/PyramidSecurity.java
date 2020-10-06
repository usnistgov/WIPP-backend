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
package gov.nist.itl.ssd.wipp.backend.data.pyramid;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

/**
 * Pyramid Security service
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Service
public class PyramidSecurity {
	
	@Autowired
	PyramidRepository pyramidRepository;
	
	public boolean checkAuthorize(String pyramidId, Boolean editMode) {
        Optional<Pyramid> pyramid = pyramidRepository.findById(pyramidId);
        if (pyramid.isPresent()){
            return(checkAuthorize(pyramid.get(), editMode));
        }
        else {
            throw new NotFoundException("Pyramid with id " + pyramidId + " not found");
        }
    }

    public static boolean checkAuthorize(Pyramid pyramid, Boolean editMode) {
        String pyramidOwner = pyramid.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!pyramid.isPubliclyShared() && (pyramidOwner == null || !pyramidOwner.equals(connectedUser))) {
            throw new ForbiddenException("You do not have access to this pyramid");
        }
        if (pyramid.isPubliclyShared() && editMode && (pyramidOwner == null || !pyramidOwner.equals(connectedUser))){
            throw new ForbiddenException("You do not have the right to edit this pyramid");
        }
        return(true);
    }

}
