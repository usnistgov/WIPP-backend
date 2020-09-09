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
package gov.nist.itl.ssd.wipp.backend.data.genericdata;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.rest.PaginationParameterTemplatesHelper;
/**
*
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
*/
@Component
public class GenericDataResourceProcessor implements RepresentationModelProcessor<EntityModel<GenericData>>{
	
	@Autowired
	private PaginationParameterTemplatesHelper assembler;

	@Autowired
	private EntityLinks entityLinks;
	
	@Override
	public EntityModel<GenericData> process(EntityModel<GenericData> resource) {
		GenericData genericData = resource.getContent();
		
        Link downloadLink = linkTo(GenericDataDownloadController.class,
        		genericData.getId())
                .withRel("download");
        resource.add(downloadLink);
        
		Link genericFilesLink = entityLinks.linkForItemResource(
				GenericData.class, genericData.getId())
				.slash("genericFile")
				.withRel("genericFile");
		resource.add(assembler.appendPaginationParameterTemplates(genericFilesLink));
        
		return resource;
	}
}
