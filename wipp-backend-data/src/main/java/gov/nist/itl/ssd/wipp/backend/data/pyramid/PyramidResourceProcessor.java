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
package gov.nist.itl.ssd.wipp.backend.data.pyramid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.PaginationParameterTemplatesHelper;
import gov.nist.itl.ssd.wipp.backend.data.pyramid.timeslices.PyramidTimeSliceController;

/**
*
* @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
*/
@Component
public class PyramidResourceProcessor implements ResourceProcessor<Resource<Pyramid>>{
	
	@Autowired
    private PaginationParameterTemplatesHelper assembler;

    @Override
    public Resource<Pyramid> process(Resource<Pyramid> resource) {
        Pyramid pyramid = resource.getContent();
        String selfUri = resource.getId().getHref();
        String pyramidBaseUri = CoreConfig.BASE_URI + "/pyramids/"
                + pyramid.getId();

        String baseUri = selfUri.replace(pyramidBaseUri,
        		CoreConfig.PYRAMIDS_BASE_URI + "/"
                + pyramid.getId());
        resource.add(new Link(baseUri, "baseUri"));

        Link timeSlicesLink = linkTo(PyramidTimeSliceController.class,
                pyramid.getId())
                .withRel("timeSlices");
        resource.add(assembler.appendPaginationParameterTemplates(
                timeSlicesLink));

        Link fetchingLink = linkTo(PyramidFetchingController.class,
                resource.getContent().getId())
                .withRel("fetching");
        resource.add(fetchingLink);

        return resource;
    }


}
