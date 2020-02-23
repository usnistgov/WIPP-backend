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
package gov.nist.itl.ssd.wipp.backend.data.csvCollection.csv;

import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author Samia Benjida <samia.benjida at nist.gov>
 */
public interface CsvRepositoryCustom {

    // When calling the deleteByImagesCollection method, which corresponds to a DELETE operation, we make sure that the user is logged in and has the right to access the object before calling the delete method
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizeCsvCollectionId(#csvCollection)")
    void deleteByCsvCollection(
            @Param("csvCollection") String csvCollection);

    // When calling the deleteByImagesCollectionAndFileName method, which corresponds to a DELETE operation, we make sure that the user is logged in and has the right to access the object before calling the delete method
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizeCsvCollectionId(#csvCollection)")
    void deleteByCsvCollectionAndFileName(
            @Param("csvCollection") String csvCollection,
            @Param("fileName") String fileName);
}
