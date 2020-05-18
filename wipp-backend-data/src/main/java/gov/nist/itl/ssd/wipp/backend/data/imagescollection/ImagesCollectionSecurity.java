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
package gov.nist.itl.ssd.wipp.backend.data.imagescollection;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

/**
 * Images Collection Security service
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Service
public class ImagesCollectionSecurity {
	
	@Autowired
    private ImagesCollectionRepository imagesCollectionRepository;

    public boolean checkAuthorize(String imagesCollectionId, Boolean editMode) {
        Optional<ImagesCollection> imagesCollection = imagesCollectionRepository.findById(imagesCollectionId);
        if (imagesCollection.isPresent()){
            return(checkAuthorize(imagesCollection.get(), editMode));
        }
        else {
            throw new NotFoundException("Image collection with id " + imagesCollectionId + " not found");
        }
    }

    public static boolean checkAuthorize(ImagesCollection imagesCollection, Boolean editMode) {
        String imagesCollectionOwner = imagesCollection.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!imagesCollection.isPubliclyShared() && (imagesCollectionOwner == null || !imagesCollectionOwner.equals(connectedUser))) {
            throw new ForbiddenException("You do not have access to this image collection");
        }
        if (imagesCollection.isPubliclyShared() && editMode && (imagesCollectionOwner == null || !imagesCollectionOwner.equals(connectedUser))){
            throw new ForbiddenException("You do not have the right to edit this image collection");
        }
        return(true);
    }

}
