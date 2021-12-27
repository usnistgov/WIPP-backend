package gov.nist.itl.ssd.wipp.backend.data.genericdata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;


/**
*
* @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
*/
@Component
public class GenericDataLogic {
	
    @Autowired
    private GenericDataRepository genericDataRepository;

    public void assertCollectionNameUnique(String name) {
        if (genericDataRepository.countByName(name) != 0) {
            throw new ClientException("A Generic Data collection named "
                    + name + " already exists.");
        }
    }
    public void assertCollectionNotLocked(GenericData genericData) {
        if (genericData.isLocked()) {
            throw new ClientException("Collection locked.");
        }
    }

    public void assertCollectionNotImporting(GenericData genericData) {
        if (genericData.getNumberImportingGenericFiles() != 0) {
            throw new ClientException("Generic Data files are still being imported.");
        }
    }

    public void assertCollectionHasNoImportError(GenericData genericData) {
        if (genericData.getNumberOfImportErrors() != 0) {
            throw new ClientException("Some Generic Data files have not been imported correctly.");
        }
    }

}
