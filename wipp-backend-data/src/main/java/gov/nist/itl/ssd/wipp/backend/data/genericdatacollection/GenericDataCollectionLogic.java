package gov.nist.itl.ssd.wipp.backend.data.genericdatacollection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;


/**
*
* @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
*/
@Component
public class GenericDataCollectionLogic {
	
    @Autowired
    private GenericDataCollectionRepository genericDataCollectionRepository;

    public void assertCollectionNameUnique(String name) {
        if (genericDataCollectionRepository.countByName(name) != 0) {
            throw new ClientException("A Generic Data collection named "
                    + name + " already exists.");
        }
    }
    public void assertCollectionNotLocked(GenericDataCollection genericDataCollection) {
        if (genericDataCollection.isLocked()) {
            throw new ClientException("Collection locked.");
        }
    }

    public void assertCollectionNotImporting(GenericDataCollection genericDataCollection) {
        if (genericDataCollection.getNumberImportingGenericFiles() != 0) {
            throw new ClientException("Generic files are still being imported.");
        }
    }

    public void assertCollectionHasNoImportError(GenericDataCollection genericDataCollection) {
        if (genericDataCollection.getNumberOfImportErrors() != 0) {
            throw new ClientException("Some Generic files have not been imported correctly.");
        }
    }

}
