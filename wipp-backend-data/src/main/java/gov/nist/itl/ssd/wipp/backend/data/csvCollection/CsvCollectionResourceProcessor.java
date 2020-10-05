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
package gov.nist.itl.ssd.wipp.backend.data.csvCollection;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import gov.nist.itl.ssd.wipp.backend.core.rest.PaginationParameterTemplatesHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
*/
@Component
public class CsvCollectionResourceProcessor implements RepresentationModelProcessor<EntityModel<CsvCollection>>{

	@Autowired
	private PaginationParameterTemplatesHelper assembler;

	@Autowired
	private EntityLinks entityLinks;

	@Override
	public EntityModel<CsvCollection> process(EntityModel<CsvCollection> resource) {
		CsvCollection csvCollection = resource.getContent();
		
        Link downloadLink = linkTo(CsvCollectionDownloadController.class,
        		csvCollection.getId())
        		.slash("request")
                .withRel("download");
        resource.add(downloadLink);

		Link csvFilesLink = entityLinks.linkForItemResource(
				CsvCollection.class, csvCollection.getId())
				.slash("csv")
				.withRel("csv");
		resource.add(assembler.appendPaginationParameterTemplates(csvFilesLink));
        
		return resource;
	}
}
	