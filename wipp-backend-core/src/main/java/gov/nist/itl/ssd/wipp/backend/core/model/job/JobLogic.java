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
package gov.nist.itl.ssd.wipp.backend.core.model.job;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Antoine Vandecreme
 */
@Component
public class JobLogic {

    @Autowired
    private JobRepository jobRepository;


    public void assertJobNameUnique(String name) {
        if (jobRepository.countByName(name) != 0) {
            throw new ClientException("A job named \""
                    + name + "\" already exists.");
        }
    }

    public void assertJobIdNull(String id) {
        if (id != null) {
            throw new ClientException(
                    "Do not specify an id when creating a new job.");
        }
    }

    public void assertNotNull(String name) {
        if (name == null) {
            throw new ClientException("You must specify a job name.");
        }
    }
    
    public void assertStatusIsCreated(Job job) {
    	if (job.getStatus() != JobStatus.CREATED) {
    		throw new ClientException("Job status does not allow for this operation.");
    	}
    }

}
