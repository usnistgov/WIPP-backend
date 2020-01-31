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

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

/**
 *
 * @author Samia Benjida <samia.benjida at nist.gov>
 */
@Component
@RepositoryEventHandler(CsvCollection.class)
public class CsvCollectionEventHandler {

    private static final Logger LOGGER = Logger.getLogger(CsvCollectionEventHandler.class.getName());

    @Autowired
    private CsvCollectionRepository csvCollectionRepository;
    @Autowired
    private CsvCollectionLogic csvCollectionLogic;

    @Autowired
    CoreConfig config;

    @HandleBeforeCreate
    public void handleBeforeCreate(CsvCollection csvCollection) {
        csvCollection.setCreationDate(new Date());
    }

    @HandleBeforeSave
    public void handleBeforeSave(CsvCollection csvCollection) {
        Optional<CsvCollection> result = csvCollectionRepository.findById(
                csvCollection.getId());
        if (!result.isPresent()) {
            throw new NotFoundException("CSV collection with id " + csvCollection.getId() + " not found");
        }

        CsvCollection oldTc = result.get();
        if (csvCollection.isLocked() != oldTc.isLocked()) {
            if (!csvCollection.isLocked()) {
                throw new ClientException("Can not unlock CSV collection.");
            }
            csvCollectionLogic.assertCollectionNotImporting(oldTc);
            csvCollectionLogic.assertCollectionHasNoImportError(oldTc);
        }}
}

