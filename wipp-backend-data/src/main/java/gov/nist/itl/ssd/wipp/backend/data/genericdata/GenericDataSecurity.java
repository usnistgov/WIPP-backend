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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

/**
 * Generic Data Security service
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Service
public class GenericDataSecurity {

	@Autowired
    private GenericDataRepository genericDataRepository;

    public boolean checkAuthorize(String genericDataId, Boolean editMode) {
        Optional<GenericData> genericData = genericDataRepository.findById(genericDataId);
        if (genericData.isPresent()){
            return(checkAuthorize(genericData.get(), editMode));
        }
        else {
            throw new NotFoundException("Generic Data with id " + genericDataId + " not found");
        }
    }

    public static boolean checkAuthorize(GenericData genericData, Boolean editMode) {
        String genericDataOwner = genericData.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!genericData.isPubliclyShared() && (genericDataOwner == null || !genericDataOwner.equals(connectedUser))) {
            throw new ForbiddenException("You do not have access to these Generic Data");
        }
        if (genericData.isPubliclyShared() && editMode && (genericDataOwner == null || !genericDataOwner.equals(connectedUser))){
            throw new ForbiddenException("You do not have the right to edit these Generic Data");
        }
        return(true);
    }
}
