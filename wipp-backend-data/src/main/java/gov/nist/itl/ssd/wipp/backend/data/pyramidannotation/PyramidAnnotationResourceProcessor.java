package gov.nist.itl.ssd.wipp.backend.data.pyramidannotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.rest.PaginationParameterTemplatesHelper;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection;
import gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.timeslices.PyramidAnnotationTimeSliceController;
import gov.nist.itl.ssd.wipp.backend.data.stitching.Resource;
import gov.nist.itl.ssd.wipp.backend.data.stitching.ResourceProcessor;
import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVector;
import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVectorRepository;
import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVectorStatisticsController;
import gov.nist.itl.ssd.wipp.backend.data.stitching.timeslices.StitchingVectorTimeSliceController;

@Component
public class PyramidAnnotationResourceProcessor implements ResourceProcessor<Resource<PyramidAnnotation>>{
	
    @Autowired
    private PyramidAnnotationRepository pyramidAnnotationRepository;

    @Autowired
    private PaginationParameterTemplatesHelper assembler;

    @Override
    public Resource<PyramidAnnotation> process(
            Resource<PyramidAnnotation> resource) {
    	PyramidAnnotation annotation = resource.getContent();

        Link link = ControllerLinkBuilder.linkTo(
                PyramidAnnotationTimeSliceController.class, annotation.getId())
                .withRel("timeSlices");
        resource.add(assembler.appendPaginationParameterTemplates(link));

        return resource;
    }

}
