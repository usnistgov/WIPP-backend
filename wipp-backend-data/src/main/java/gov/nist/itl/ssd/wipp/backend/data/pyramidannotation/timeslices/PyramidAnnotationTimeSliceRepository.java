package gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.timeslices;

import java.io.File;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVector;
import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVectorConfig;
import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVectorRepository;
import gov.nist.itl.ssd.wipp.backend.data.stitching.timeslices.StitchingVectorTimeSlice;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@Component
public class PyramidAnnotationTimeSliceRepository {
	
    @Autowired
    private CoreConfig config;
    
    @Autowired
    private PyramidAnnotationRepository pyramidAnnotationRepository;

    public StitchingVectorTimeSlice findOne(String stitchingVectorId,
            int timeSlice) {
        return pyramidAnnotationRepository.getTimeSlices(stitchingVectorId)
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
