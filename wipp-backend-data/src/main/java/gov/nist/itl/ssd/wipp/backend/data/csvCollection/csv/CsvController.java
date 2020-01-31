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

/**
 *
 * @author Samia Benjida <samia.benjida at nist.gov>
 */
@RestController
//@Api(tags="CsvCollection Entity")
@RequestMapping(CoreConfig.BASE_URI + "/csvCollections/{csvCollectionId}/csv")
@ExposesResourceFor(Csv.class)
public class CsvController {

    @Autowired
    private CsvRepository csvRepository;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public HttpEntity<PagedResources<Resource<Csv>>> getFilesPage(
            @PathVariable("csvCollectionId") String csvCollectionId,
            @PageableDefault Pageable pageable,
            PagedResourcesAssembler<Csv> assembler) {
        Page<Csv> files = csvRepository.findByCsvCollection(
                csvCollectionId, pageable);
        PagedResources<Resource<Csv>> resources = assembler.toResource(files);

        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

}
