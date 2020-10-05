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
package gov.nist.itl.ssd.wipp.backend.core.model.data;

import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;

/**
 * Default DataHandler for unsupported data types
 *
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Component("defaultDataHandler")
public class DefaultDataHandler implements DataHandler {

    @Override
    public void importData(Job job, String outputName) throws Exception {
        throw new Exception("Unsupported data type, unable to import.");

    }

    @Override
    public String exportDataAsParam(String value) {
        return value;
    }
    
    @Override
    public void setDataToPublic(String value) {
    	// nothing to be done for non-wipp data
    }

}