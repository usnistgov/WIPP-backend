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

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Iterative training Pipeline Security service
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Service
public class IterativeTrainingPipelineSecurity {
	
	@Autowired
    private IterativeTrainingPipelineRepository iterativeTrainingPipelineRepository;

    public boolean checkAuthorize(String iterativeTrainingPipelineId, Boolean editMode) {
        Optional<IterativeTrainingPipeline> iterativeTrainingPipeline = iterativeTrainingPipelineRepository.findById(iterativeTrainingPipelineId);
        if (iterativeTrainingPipeline.isPresent()){
            return(checkAuthorize(iterativeTrainingPipeline.get(), editMode));
        }
        else {
            throw new NotFoundException("Iterative training pipeline with id " + iterativeTrainingPipelineId + " not found");
        }
    }

    public static boolean checkAuthorize(IterativeTrainingPipeline iterativeTrainingPipeline, Boolean editMode) {
        String iterativeTrainingPipelineOwner = iterativeTrainingPipeline.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!iterativeTrainingPipeline.isPubliclyShared() && (iterativeTrainingPipelineOwner == null || !iterativeTrainingPipelineOwner.equals(connectedUser))) {
            throw new ForbiddenException("You do not have access to this pipeline");
        }
        if (iterativeTrainingPipeline.isPubliclyShared() && editMode && (iterativeTrainingPipelineOwner == null || !iterativeTrainingPipelineOwner.equals(connectedUser))){
            throw new ForbiddenException("You do not have the right to edit this pipeline");
        }
        return(true);
    }

}
