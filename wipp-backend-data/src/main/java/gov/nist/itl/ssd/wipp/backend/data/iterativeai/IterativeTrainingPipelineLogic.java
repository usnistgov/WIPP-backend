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

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Iterative AI training pipeline logic
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Component
public class IterativeTrainingPipelineLogic {

    @Autowired
    private IterativeTrainingPipelineRepository iterativeTrainingPipelineRepository;

    public void assertIterativeTrainingPipelineNameUnique(String name) {
        if (iterativeTrainingPipelineRepository.countByName(name) != 0) {
            throw new ClientException("A training pipeline named \""
                    + name + "\" already exists.");
        }
    }
}
