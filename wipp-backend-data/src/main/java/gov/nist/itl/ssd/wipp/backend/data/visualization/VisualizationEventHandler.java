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

import java.util.Date;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 */
@Component
@RepositoryEventHandler(Visualization.class)
public class VisualizationEventHandler {

    // We make sure the user trying to create a visualization is logged in.
    @PreAuthorize("@securityServiceData.hasUserRole()")
    @HandleBeforeCreate
    public void handleBeforeCreate(Visualization visualization) {
        visualization.setCreationDate(new Date());
        // We set the owner to the connected user
        visualization.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    // TODO : if needed, add the handleBeforeSave method and make sure a public visualization can not be made private (cf. ImagesCollectionEventHandler class)
}
