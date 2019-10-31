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

import java.util.List;

import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.AccessType.Type;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

/**
 * Workaround for https://jira.spring.io/browse/DATAREST-1363
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public abstract class CustomResourceSupport extends ResourceSupport {

    @AccessType(Type.PROPERTY)
    public void setLinks(List<Link> links) {
        List<Link> actual = super.getLinks();
        actual.clear();
        actual.addAll(links);
    }
}
