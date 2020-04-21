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
package gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.timeslices;

import java.io.File;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.PyramidAnnotation;
import gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.PyramidAnnotationConfig;
import gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.PyramidAnnotationRepository;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@Component
public class PyramidAnnotationTimeSliceRepository {
	
    @Autowired
    private CoreConfig config;
    
    @Autowired
    private PyramidAnnotationRepository pyramidAnnotationRepository;

    public PyramidAnnotationTimeSlice findOne(String pyramidAnnotationId,
            int timeSlice) {
        return pyramidAnnotationRepository.getTimeSlices(pyramidAnnotationId)
                .stream()
                .filter(pats -> pats.getSliceNumber() == timeSlice)
                .findFirst()
                .orElse(null);
    }

    public File getGlobalPositionsFile(String pyramidAnnotationId, int timeSlice) {
        PyramidAnnotation pyramidAnnotation = null;
        Optional<PyramidAnnotation> optionalPyramidAnnotation = pyramidAnnotationRepository.findById(
        		pyramidAnnotationId);
        if(optionalPyramidAnnotation.isPresent()) {
        	pyramidAnnotation = optionalPyramidAnnotation.get();
        } else {
            throw new NotFoundException("Pyramid annotation " + pyramidAnnotationId + "not found.");
        }

        int maxTimeSlice = pyramidAnnotation.getTimeSlices().stream()
                .mapToInt(pa -> pa.getSliceNumber()).max().orElse(0);
        int nbDigits = Integer.toString(maxTimeSlice).length();
        String fileName = String.format("%s%0" + nbDigits + "d%s",
                PyramidAnnotationConfig.PYRAMID_ANNOTATION_FILENAME_PREFIX,
                timeSlice,
                PyramidAnnotationConfig.PYRAMID_ANNOTATION_FILENAME_SUFFIX);
        
        File pyramidAnnotationFolder = new File(config.getPyramidAnnotationsFolder(), pyramidAnnotation.getId());
        File gPFile = new File(pyramidAnnotationFolder, fileName);
        
        if(gPFile.exists()){
        	return gPFile;
        }else{
        	fileName = PyramidAnnotationConfig.PYRAMID_ANNOTATION_FILENAME_PREFIX + timeSlice + PyramidAnnotationConfig.PYRAMID_ANNOTATION_FILENAME_SUFFIX;
        	gPFile = new File(pyramidAnnotationFolder, fileName);
        	return gPFile;
        }
    }

}
