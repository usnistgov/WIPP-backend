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
package gov.nist.itl.ssd.wipp.backend.core.rest;

import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.UriTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 * This helper adds pagination parameters templates to links, since it was removed from
 * method appendPaginationParameterTemplates of class PageableHandlerMethodArgumentResolver
 * as of Spring-data 1.11
 * See issue http://stackoverflow.com/questions/34518885/how-to-add-pagination-templates-in-the-links-with-spring-data-1-11
 */
@Service
public class PaginationParameterTemplatesHelper {

	private final HateoasPageableHandlerMethodArgumentResolver pageableResolver;
	
	public PaginationParameterTemplatesHelper() {
		this.pageableResolver = new HateoasPageableHandlerMethodArgumentResolver();
	}

	public Link appendPaginationParameterTemplates(Link link) {
		 
 		Assert.notNull(link, "Link must not be null!");

		String uri = link.getHref();
		UriTemplate uriTemplate = UriTemplate.of(uri);
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(uri).build();
		TemplateVariables variables = pageableResolver.getPaginationTemplateVariables(null, uriComponents);

		return new Link(uriTemplate.with(variables), link.getRel());

 	}
}
