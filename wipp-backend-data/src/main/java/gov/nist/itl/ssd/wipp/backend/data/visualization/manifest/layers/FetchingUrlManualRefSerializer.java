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
package gov.nist.itl.ssd.wipp.backend.data.visualization.manifest.layers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.io.IOException;

import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import gov.nist.itl.ssd.wipp.backend.data.pyramid.PyramidFetchingController;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Component
public class FetchingUrlManualRefSerializer extends JsonSerializer<String> {
	
	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider sp)
			throws IOException, JsonProcessingException {
		if (value != null) {
			Link fetchingLink = linkTo(PyramidFetchingController.class,
	                value)
	                .withRel("fetching");
			if (fetchingLink != null) {
				gen.writeString(fetchingLink.getHref());
			}
		}
		
	}
}
