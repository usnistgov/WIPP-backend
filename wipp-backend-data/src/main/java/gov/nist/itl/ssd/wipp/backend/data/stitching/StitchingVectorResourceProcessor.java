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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.data.stitching.timeslices.StitchingVectorTimeSliceController;
import gov.nist.itl.ssd.wipp.backend.core.rest.PaginationParameterTemplatesHelper;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 */
@Component
public class StitchingVectorResourceProcessor
        implements ResourceProcessor<Resource<StitchingVector>> {

    @Autowired
    private StitchingVectorRepository stitchingVectorRepository;

    @Autowired
    private PaginationParameterTemplatesHelper assembler;

    @Override
    public Resource<StitchingVector> process(
            Resource<StitchingVector> resource) {
        StitchingVector vector = resource.getContent();

        Link link = ControllerLinkBuilder.linkTo(
                StitchingVectorTimeSliceController.class, vector.getId())
                .withRel("timeSlices");
        resource.add(assembler.appendPaginationParameterTemplates(link));

        if (stitchingVectorRepository.getStatisticsFile(
                vector.getId()).exists()) {
            link = ControllerLinkBuilder.linkTo(
                    StitchingVectorStatisticsController.class, vector.getId())
                    .withRel("statistics");
            resource.add(link);
        }

        return resource;
    }

}
