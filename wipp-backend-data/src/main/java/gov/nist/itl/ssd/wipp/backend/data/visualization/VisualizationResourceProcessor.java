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
package gov.nist.itl.ssd.wipp.backend.data.visualization;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.springframework.stereotype.Component;

/**
 *
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Component
public class VisualizationResourceProcessor
        implements RepresentationModelProcessor<EntityModel<Visualization>> {

    @Override
    public EntityModel<Visualization> process(EntityModel<Visualization> resource) {

        Link downloadLink = linkTo(VisualizationDownloadController.class,
                resource.getContent().getId())
                .withRel("download");
        resource.add(downloadLink);

        return resource;
    }

}
