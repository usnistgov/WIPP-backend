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

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for DataHandlers
 *
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Service
public class DataHandlerService {

    @Autowired
    private DataHandlerFactory dataHandlerFactory;

    public DataHandler getDataHandler(String dataType) {
        DataHandler dataHandler;
        // try to get specific data handler for dataType, if not found switch to default
        try {
            dataHandler = dataHandlerFactory.getDataHandler(dataType + "DataHandler");
        } catch(NoSuchBeanDefinitionException ex) {
            dataHandler = dataHandlerFactory.getDataHandler("defaultDataHandler");
        }
        return dataHandler;
    }
}