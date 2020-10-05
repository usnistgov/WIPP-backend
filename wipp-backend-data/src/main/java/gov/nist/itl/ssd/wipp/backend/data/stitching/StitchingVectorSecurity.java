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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

/**
 * Stitching Vector Security service
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Service
public class StitchingVectorSecurity {
	
	@Autowired
    private StitchingVectorRepository stitchingVectorRepository;

    public boolean checkAuthorize(String stitchingVectorId, Boolean editMode) {
        Optional<StitchingVector> stitchingVector = stitchingVectorRepository.findById(stitchingVectorId);
        if (stitchingVector.isPresent()){
            return(checkAuthorize(stitchingVector.get(), editMode));
        }
        else {
            throw new NotFoundException("Stitching vector with id " + stitchingVectorId + " not found");
        }
    }

    public static boolean checkAuthorize(StitchingVector stitchingVector, Boolean editMode) {
        String stitchingVectorOwner = stitchingVector.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!stitchingVector.isPubliclyShared() && (stitchingVectorOwner == null || !stitchingVectorOwner.equals(connectedUser))) {
            throw new ForbiddenException("You do not have access to this stitching vector");
        }
        if (stitchingVector.isPubliclyShared() && editMode && (stitchingVectorOwner == null || !stitchingVectorOwner.equals(connectedUser))){
            throw new ForbiddenException("You do not have the right to edit this stitching vector");
        }
        return(true);
    }

}
