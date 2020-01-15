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
package gov.nist.itl.ssd.wipp.backend.data.csvCollection;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samia Benjida <samia.benjida at nist.gov>
 */
@Component
public class CsvCollectionLogic {

    @Autowired
    private CsvCollectionRepository csvCollectionRepository;

    public void assertCollectionNotLocked(CsvCollection csvCollection) {
        if (csvCollection.isLocked()) {
            throw new ClientException("Collection locked.");
        }
    }

    public void assertCollectionNotImporting(CsvCollection csvCollection) {
        if (csvCollection.getNumberImportingCsv() != 0) {
            throw new ClientException("CSV are still being imported.");
        }
    }

    public void assertCollectionHasNoImportError(CsvCollection csvCollection) {
        if (csvCollection.getNumberOfImportErrors() != 0) {
            throw new ClientException("Some CSV have not been imported correctly.");
        }
    }

    public void assertCollectionNameUnique(String name) {
        if (csvCollectionRepository.countByName(name) != 0) {
            throw new ClientException("A CSV collection named \""
                    + name + "\" already exists.");
        }
    }

}
