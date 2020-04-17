package gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.timeslices;

import gov.nist.itl.ssd.wipp.backend.core.rest.CustomResourceSupport;

public class PyramidAnnotationTimeSlice extends CustomResourceSupport{
	
    private final int sliceNumber;

    public PyramidAnnotationTimeSlice(int sliceNumber) {
        this.sliceNumber = sliceNumber;
    }

    public int getSliceNumber() {
        return sliceNumber;
    }
}
