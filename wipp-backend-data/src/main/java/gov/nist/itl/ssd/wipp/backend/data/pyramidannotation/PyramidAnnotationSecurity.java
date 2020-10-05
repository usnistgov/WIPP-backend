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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

/**
 * Pyramid Annotation Security service
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Service
public class PyramidAnnotationSecurity {
	
	@Autowired
	PyramidAnnotationRepository pyramidAnnotationRepository;
	
	public boolean checkAuthorize(String pyramidAnnotationId, Boolean editMode) {
        Optional<PyramidAnnotation> pyramidAnnotation = pyramidAnnotationRepository.findById(pyramidAnnotationId);
        if (pyramidAnnotation.isPresent()){
            return(checkAuthorize(pyramidAnnotation.get(), editMode));
        }
        else {
            throw new NotFoundException("Pyramid Annotation with id " + pyramidAnnotationId + " not found");
        }
    }

    public static boolean checkAuthorize(PyramidAnnotation pyramidAnnotation, Boolean editMode) {
        String pyramidAnnotationOwner = pyramidAnnotation.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!pyramidAnnotation.isPubliclyShared() && (pyramidAnnotationOwner == null || !pyramidAnnotationOwner.equals(connectedUser))) {
            throw new ForbiddenException("You do not have access to this pyramid annotation");
        }
        if (pyramidAnnotation.isPubliclyShared() && editMode && (pyramidAnnotationOwner == null || !pyramidAnnotationOwner.equals(connectedUser))){
            throw new ForbiddenException("You do not have the right to edit this pyramid annotation");
        }
        return(true);
    }

}
