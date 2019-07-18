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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.net.URI;

/**
 *
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
public class FetchingUrlManualRefDeserializer extends JsonDeserializer<String> {

    public FetchingUrlManualRefDeserializer() {
    }

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        URI uri = ctxt.readValue(jp, URI.class);

        if (uri != null) {
	        String[] parts = uri.getPath().split("/");
	        return parts[parts.length - 2];
        }
        
        return null;
    }
}
