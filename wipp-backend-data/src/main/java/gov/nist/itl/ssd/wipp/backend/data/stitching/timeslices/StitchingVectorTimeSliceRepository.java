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
package gov.nist.itl.ssd.wipp.backend.data.stitching.timeslices;

import java.io.File;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVector;
import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVectorConfig;
import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVectorRepository;
import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

/**
 *
 * @author Antoine Vandecreme
 * Adapted by Mohamed Ouladi <mohamed.ouladi@nist.gov>
 */
@Component
public class StitchingVectorTimeSliceRepository {

    @Autowired
    private CoreConfig config;

    @Autowired
    private StitchingVectorRepository stitchingVectorRepository;

    public StitchingVectorTimeSlice findOne(String stitchingVectorId,
            int timeSlice) {
        return stitchingVectorRepository.getTimeSlices(stitchingVectorId)
                .stream()
                .filter(svts -> svts.getSliceNumber() == timeSlice)
                .findFirst()
                .orElse(null);
    }

    public File getGlobalPositionsFile(String stitchingVectorId, int timeSlice) {
        StitchingVector stitchingVector = null;
        Optional<StitchingVector> optionalStitchingVector = stitchingVectorRepository.findById(
                stitchingVectorId);
        if(optionalStitchingVector.isPresent()) {
            stitchingVector = optionalStitchingVector.get();
        } else {
            throw new NotFoundException("Stitching vector " + stitchingVectorId + "not found.");
        }

        int maxTimeSlice = stitchingVector.getTimeSlices().stream()
                .mapToInt(sv -> sv.getSliceNumber()).max().orElse(0);
        int nbDigits = Integer.toString(maxTimeSlice).length();
        String fileName = String.format("%s%0" + nbDigits + "d%s",
                StitchingVectorConfig.STITCHING_VECTOR_GLOBAL_POSITION_PREFIX,
                timeSlice,
                StitchingVectorConfig.STITCHING_VECTOR_FILENAME_SUFFIX);
        
        File stitchingFolder = new File(config.getStitchingFolder(), stitchingVector.getId());
        File gPFile = new File(stitchingFolder, fileName);
        
        if(gPFile.exists()){
        	return gPFile;
        }else{
        	fileName = StitchingVectorConfig.STITCHING_VECTOR_GLOBAL_POSITION_PREFIX + timeSlice + StitchingVectorConfig.STITCHING_VECTOR_FILENAME_SUFFIX;
        	gPFile = new File(stitchingFolder, fileName);
        	return gPFile;
        }
    }
}
