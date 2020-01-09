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
//import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Samia Benjida <samia.benjida at nist.gov>
 */
@RestController
//@Api(tags="Csv collection Entity")
@RequestMapping(CoreConfig.BASE_URI + "/csvCollections/upload")
public class CsvCollectionUploadController {

    @Autowired
    private CoreConfig config;

    @Autowired
    private CsvCollectionRepository csvCollectionRepository;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public CsvCollection upload(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("name") String name)
            throws IOException {
        if (name == null || name.isEmpty()) {
            throw new ClientException(
                    "A csv collection name must be specified.");
        }
        assertCollectionNameUnique(name);
        CsvCollection csvCollection = new CsvCollection(name);
        csvCollection = csvCollectionRepository.save(csvCollection);
        File csvCollectionFolder = new File(
                config.getCsvCollectionsFolder(),
                csvCollection.getId());
        csvCollectionFolder.mkdirs();
        for (MultipartFile file : files) {
            file.transferTo(new File(csvCollectionFolder, file.getOriginalFilename()));
        }
        return csvCollection;
    }

    public void assertCollectionNameUnique(String name) {
        if (csvCollectionRepository.countByName(name) != 0) {
            throw new ClientException("A CSV collection named \""
                    + name + "\" already exists.");
        }
    }

}