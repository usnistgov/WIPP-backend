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

import gov.nist.itl.ssd.wipp.backend.core.rest.CustomResourceSupport;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
public class PyramidAnnotationTimeSlice extends CustomResourceSupport {
	
    private final int sliceNumber;

    public PyramidAnnotationTimeSlice(int sliceNumber) {
        this.sliceNumber = sliceNumber;
    }

    public int getSliceNumber() {
        return sliceNumber;
    }
}
