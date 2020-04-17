package gov.nist.itl.ssd.wipp.backend.data.pyramidannotation;

import java.util.List;

import gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.timeslices.PyramidAnnotationTimeSlice;

public interface PyramidAnnotationRepositoryCustom {
	
    void setTimeSlices(String pyramidAnnotationId,
            List<PyramidAnnotationTimeSlice> timeSlices);

    List<PyramidAnnotationTimeSlice> getTimeSlices(String pyramidAnnotationId);

}
