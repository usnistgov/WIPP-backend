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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;

/**
 *
 * @author Antoine Vandecreme
 */
@Component
public class ImagesCollectionLogic {

    @Autowired
    private ImagesCollectionRepository imagesCollectionRepository;

    public void assertCollectionNotLocked(ImagesCollection imagesCollection) {
        if (imagesCollection.isLocked()) {
            throw new ClientException("Collection locked.");
        }
    }

    public void assertCollectionNotImporting(ImagesCollection imagesCollection) {
        if (imagesCollection.getNumberImportingImages() != 0) {
            throw new ClientException("Images are still being imported.");
        }
    }

    public void assertCollectionHasNoImportError(ImagesCollection imagesColleciton) {
        if (imagesColleciton.getNumberOfImportErrors() != 0) {
            throw new ClientException("Some images have not been imported correctly.");
        }
    }

    public void assertCollectionNameUnique(String name) {
        if (imagesCollectionRepository.countByName(name) != 0) {
            throw new ClientException("An images collection named \""
                    + name + "\" already exists.");
        }
    }

}
