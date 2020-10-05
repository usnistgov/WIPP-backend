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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

/**
 * CSV Collection Security service
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Service
public class CsvCollectionSecurity {
	
	@Autowired
    private CsvCollectionRepository csvCollectionRepository;

    public boolean checkAuthorize(String csvCollectionId, Boolean editMode) {
        Optional<CsvCollection> csvCollection = csvCollectionRepository.findById(csvCollectionId);
        if (csvCollection.isPresent()){
            return(checkAuthorize(csvCollection.get(), editMode));
        }
        else {
            throw new NotFoundException("CSV collection with id " + csvCollectionId + " not found");
        }
    }

    public static boolean checkAuthorize(CsvCollection csvCollection, Boolean editMode) {
        String csvCollectionOwner = csvCollection.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!csvCollection.isPubliclyShared() && (csvCollectionOwner == null || !csvCollectionOwner.equals(connectedUser))) {
            throw new ForbiddenException("You do not have access to this CSV collection");
        }
        if (csvCollection.isPubliclyShared() && editMode && (csvCollectionOwner == null || !csvCollectionOwner.equals(connectedUser))){
            throw new ForbiddenException("You do not have the right to edit this CSV collection");
        }
        return(true);
    }

}
