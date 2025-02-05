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
package gov.nist.itl.ssd.wipp.backend.data.iterativeai;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Component
@RepositoryEventHandler(IterativeTrainingPipeline.class)
public class IterativeTrainingPipelineEventHandler {

    @Autowired
    IterativeTrainingPipelineRepository iterativeTrainingPipelineRepository;

    @Autowired
    IterativeTrainingPipelineLogic iterativeTrainingPipelineLogic;

    @Autowired
    CoreConfig config;

    @PreAuthorize("isAuthenticated()")
    @HandleBeforeCreate
    public void handleBeforeCreate(IterativeTrainingPipeline iterativeTrainingPipeline) {
        // Assert name is unique
        iterativeTrainingPipelineLogic.assertIterativeTrainingPipelineNameUnique(
                iterativeTrainingPipeline.getName());

        // Set creation date to current date
        iterativeTrainingPipeline.setCreationDate(new Date());

        // Set the owner to the connected user
        iterativeTrainingPipeline.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());

        // Default task category is SEGMENTATION
        if (iterativeTrainingPipeline.getTaskCategory() == null) {
            iterativeTrainingPipeline.setTaskCategory(IterativeTrainingPipeline.TaskCategory.SEGMENTATION);
        }

        // Iterations cannot be set at creation time
        if (iterativeTrainingPipeline.getIterations() != null) {
            throw new ClientException("Training iterations cannot be set at creation time.");
        };

        // Create collection for generated ground truth if not specified (handled separately for now)
//        if(iterativeTrainingPipeline.getGroundTruthCollection()== null) {
//        }
    }

    @HandleBeforeSave
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #iterativeTrainingPipeline.owner == authentication.name)")
    public void handleBeforeSave(IterativeTrainingPipeline iterativeTrainingPipeline) {
        // Assert data exists
        Optional<IterativeTrainingPipeline> result = iterativeTrainingPipelineRepository.findById(
                iterativeTrainingPipeline.getId());
        if (!result.isPresent()) {
            throw new NotFoundException("IterativeTrainingPipeline with id " + iterativeTrainingPipeline.getId() + " not found");
        }

        IterativeTrainingPipeline oldItp = result.get();

        // Assert data name is unique
        if (!Objects.equals(iterativeTrainingPipeline.getName(), oldItp.getName())) {
            iterativeTrainingPipelineLogic.assertIterativeTrainingPipelineNameUnique(
                    iterativeTrainingPipeline.getName());
        }

        // A public data cannot become private
        if (oldItp.isPubliclyShared() && !iterativeTrainingPipeline.isPubliclyShared()){
            throw new ClientException("Can not change a public data to private.");
        }

        // Owner cannot be changed
        if (!Objects.equals(
                iterativeTrainingPipeline.getOwner(),
                oldItp.getOwner())) {
            throw new ClientException("Can not change owner.");
        }

        // Iterations cannot be modified here - use iterations endpoints instead
        if (!Objects.equals(
                iterativeTrainingPipeline.getIterations(),
                oldItp.getIterations())) {
            throw new ClientException("Training iterations cannot be modified while modifying pipeline object.");
        };
    }

}
