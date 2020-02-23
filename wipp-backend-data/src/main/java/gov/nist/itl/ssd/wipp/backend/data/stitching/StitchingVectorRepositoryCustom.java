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

import com.mongodb.lang.NonNull;
import gov.nist.itl.ssd.wipp.backend.data.stitching.timeslices.StitchingVectorTimeSlice;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.File;
import java.util.List;

/**
*
* @author Antoine Vandecreme
*/
public interface StitchingVectorRepositoryCustom {

    // Only logged in user with access to the stitching vector can set it's time slices
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizeStitchingVectorId(#stitchingVectorId)")
    void setTimeSlices(@Param("stitchingVectorId") String stitchingVectorId,
                       @Param("timeSlices") List<StitchingVectorTimeSlice> timeSlices);

    // We make sure that the user trying to get the time slices of a stitching vector has the right to access it
    @PreAuthorize("@securityServiceData.checkAuthorizeStitchingVectorId(#stitchingVectorId)")
    List<StitchingVectorTimeSlice> getTimeSlices(@Param("stitchingVectorId") String stitchingVectorId);

    // We make sure that the user trying to get the statistics of a stitching vector has the right to access it
    @PreAuthorize("@securityServiceData.checkAuthorizeStitchingVectorId(#stitchingVectorId)")
    File getStatisticsFile(@Param("stitchingVectorId") String stitchingVectorId);

}