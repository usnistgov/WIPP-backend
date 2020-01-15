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

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.data.pyramid.Pyramid;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Component
public class BaseUrlManualRefSerializer extends JsonSerializer<String> {

	private static EntityLinks entityLinks;
	
	public BaseUrlManualRefSerializer() {}
	
	@Autowired
    public BaseUrlManualRefSerializer(EntityLinks entityLinks) {
        BaseUrlManualRefSerializer.entityLinks = entityLinks;
    }
	
	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider sp)
			throws IOException, JsonProcessingException {
		if (value != null) {
			Link link = entityLinks.linkToSingleResource(
                    Pyramid.class,
                    value
            );
			
			if (link != null) {
				// replace standard link by custom one for pyramids
				String selfUri = link.getHref();
		        String pyramidBaseUri = CoreConfig.BASE_URI + "/pyramids/"
		                + value;

		        String baseUri = selfUri.replace(pyramidBaseUri,
		        		CoreConfig.PYRAMIDS_BASE_URI + "/"
		                + value);
				gen.writeString(baseUri);
			}
		}
		
	}

}
