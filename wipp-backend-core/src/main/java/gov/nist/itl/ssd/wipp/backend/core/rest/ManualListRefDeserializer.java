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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ManualListRefDeserializer extends JsonDeserializer<List<String>> {

    public ManualListRefDeserializer() {
    }

    @Override
    public List<String> deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        List<?> uriList = ctxt.readValue(jp, List.class);
        List<String> parts = new ArrayList<>();

        for(Object uri: uriList) {
            String[] uriParts = ((String) uri).split("/");
            parts.add(uriParts[uriParts.length - 1]);
        }

        return parts;
    }
}
