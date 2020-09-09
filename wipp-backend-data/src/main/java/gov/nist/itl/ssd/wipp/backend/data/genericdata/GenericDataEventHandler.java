package gov.nist.itl.ssd.wipp.backend.data.genericdata;

import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi@nist.gov>
*/
@Component
@RepositoryEventHandler(GenericData.class)
public class GenericDataEventHandler {

    private static final Logger LOGGER = Logger.getLogger(GenericDataEventHandler.class.getName());

    @Autowired
    private GenericDataRepository genericDataRepository;
    @Autowired
    private GenericDataLogic genericDataLogic;

    @Autowired
    CoreConfig config;

    @HandleBeforeCreate
    public void handleBeforeCreate(GenericData genericData) {
    	genericData.setCreationDate(new Date());
    }

    @HandleBeforeSave
    public void handleBeforeSave(GenericData genericData) {
        Optional<GenericData> result = genericDataRepository.findById(
        		genericData.getId());
        if (!result.isPresent()) {
            throw new NotFoundException("Generic data collection with id " + genericData.getId() + " not found");
        }

        GenericData oldTc = result.get();
        if (genericData.isLocked() != oldTc.isLocked()) {
            if (!genericData.isLocked()) {
                throw new ClientException("Can not unlock Generic Data collection.");
            }
            genericDataLogic.assertCollectionNotImporting(oldTc);
            genericDataLogic.assertCollectionHasNoImportError(oldTc);
        }}
}
