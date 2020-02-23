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

import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author Antoine Vandecreme
 */
public interface ImagesCollectionRepositoryCustom {
    /**
     *
     * We check before updating images/metadata caches that the user is connected and has the right to access the collection
     */
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizeImagesCollectionId(#imagesCollectionId)")
    void updateImagesCaches(@Param("imagesCollectionId") String imagesCollectionId);

    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizeImagesCollectionId(#imagesCollectionId)")
    void updateMetadataFilesCaches(@Param("imagesCollectionId") String imagesCollectionId);

}
