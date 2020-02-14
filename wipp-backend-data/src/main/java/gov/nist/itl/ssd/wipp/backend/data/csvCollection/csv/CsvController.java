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
package gov.nist.itl.ssd.wipp.backend.data.csvCollection.csv;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.CsvCollection;
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.CsvCollectionRepository;

import io.swagger.annotations.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 *
 * @author Samia Benjida <samia.benjida at nist.gov>
 */
@RestController
@Api(tags="CsvCollection Entity")
@RequestMapping(CoreConfig.BASE_URI + "/csvCollections/{csvCollectionId}/csv")
@ExposesResourceFor(Csv.class)
public class CsvController {

    @Autowired
    private EntityLinks entityLinks;

    @Autowired
    private CsvRepository csvRepository;

    @Autowired
    private CsvCollectionRepository csvCollectionRepository;

    @Autowired
    private CsvHandler csvHandler;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public HttpEntity<PagedResources<Resource<Csv>>> getFilesPage(
            @PathVariable("csvCollectionId") String csvCollectionId,
            @PageableDefault Pageable pageable,
            PagedResourcesAssembler<Csv> assembler) {
        Page<Csv> files = csvRepository.findByCsvCollection(
                csvCollectionId, pageable);
        PagedResources<Resource<Csv>> resources
                = assembler.toResource(files);
        resources.forEach(
                resource -> processResource(csvCollectionId, resource));
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public void deleteAllFiles(
            @PathVariable("csvCollectionId") String csvCollectionId) {
        Optional<CsvCollection> tc =csvCollectionRepository.findById(
                csvCollectionId);
        if (!tc.isPresent()) {
            throw new NotFoundException("Collection not found");
        }
        if (tc.get().isLocked()) {
            throw new ClientException("Collection locked.");
        }
        csvHandler.deleteAll(csvCollectionId);
    }

    @RequestMapping(value = "/{fileName:.+}", method = RequestMethod.DELETE)
    public void deleteFile(
            @PathVariable("csvCollectionId") String csvCollectionId,
            @PathVariable("fileName") String fileName) {
        Optional<CsvCollection> tc = csvCollectionRepository.findById(
                csvCollectionId);
        if (!tc.isPresent()) {
            throw new NotFoundException("Collection not found");
        }
        if (tc.get().isLocked()) {
            throw new ClientException("Collection locked.");
        }
        csvHandler.delete(csvCollectionId, fileName);
    }

    protected void processResource(String csvCollectionId,
                                   Resource<Csv> resource) {
        Csv file = resource.getContent();
        Link link = entityLinks.linkForSingleResource(
                CsvCollection.class, csvCollectionId)
                .slash("csv")
                .slash(file.getFileName())
                .withSelfRel();
        resource.add(link);
    }

}
