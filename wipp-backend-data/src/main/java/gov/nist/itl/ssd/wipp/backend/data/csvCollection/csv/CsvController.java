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
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataDownloadToken;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataDownloadTokenRepository;
import gov.nist.itl.ssd.wipp.backend.core.rest.DownloadUrl;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.CsvCollection;
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.CsvCollectionRepository;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 *
 * @author Samia Benjida <samia.benjida at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@RestController
@Tag(name="CsvCollection Entity")
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

    @Autowired
    private CoreConfig coreConfig;

    @Autowired
    private DataDownloadTokenRepository dataDownloadTokenRepository;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @PreAuthorize("hasRole('admin') or @csvCollectionSecurity.checkAuthorize(#csvCollectionId, false)")
    public HttpEntity<PagedModel<EntityModel<Csv>>> getFilesPage(
            @PathVariable("csvCollectionId") String csvCollectionId,
            @ParameterObject @PageableDefault Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Csv> assembler) {
        Page<Csv> files = csvRepository.findByCsvCollection(
                csvCollectionId, pageable);
        PagedModel<EntityModel<Csv>> resources
                = assembler.toModel(files);
        resources.forEach(
                resource -> processResource(csvCollectionId, resource));
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('admin') or @csvCollectionSecurity.checkAuthorize(#csvCollectionId, true))")
    public void deleteAllFiles(
            @PathVariable("csvCollectionId") String csvCollectionId) {
        checkBeforeDelete(csvCollectionId);
        csvHandler.deleteAll(csvCollectionId);
    }

    @RequestMapping(value = "/{fileName:.+}", method = RequestMethod.DELETE)
    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('admin') or @csvCollectionSecurity.checkAuthorize(#csvCollectionId, true))")
    public void deleteFile(
            @PathVariable("csvCollectionId") String csvCollectionId,
            @PathVariable("fileName") String fileName) {
        checkBeforeDelete(csvCollectionId);
        csvHandler.delete(csvCollectionId, fileName);
    }

    private void checkBeforeDelete(String csvCollectionId) {
        Optional<CsvCollection> tc = csvCollectionRepository.findById(
                csvCollectionId);
        if (!tc.isPresent()) {
            throw new NotFoundException("Collection not found");
        }
        if (tc.get().isLocked()) {
            throw new ClientException("Collection locked.");
        }
    }

    protected void processResource(String csvCollectionId,
                                   EntityModel<Csv> resource) {
        Csv file = resource.getContent();
        Link link = entityLinks.linkForItemResource(
                        CsvCollection.class, csvCollectionId)
                .slash("csv")
                .slash(file.getFileName())
                .withSelfRel();
        resource.add(link);
    }

    // APPROACH 1: BYTE[]
    @RequestMapping(
            value="/{fileName:.+}/content",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or @csvCollectionSecurity.checkAuthorize(#csvCollectionId, true))")
    public ResponseEntity<byte[]> getContent(
            @PathVariable("csvCollectionId") String csvCollectionId,
            @PathVariable("fileName") String fileName) throws IOException
    {
        String path = coreConfig.getCsvCollectionsFolder() + "/" + csvCollectionId + "/" + fileName;
        byte[] csvBytes = Files.readAllBytes(Paths.get(path));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("text/csv"));
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename("file.csv").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    @RequestMapping(
            value = "/{fileName:.+}/downloadRequest",
            method = RequestMethod.GET,
            produces = "application/json")
    @PreAuthorize("hasRole('admin') or @csvCollectionSecurity.checkAuthorize(#csvCollectionId, false)")
    public DownloadUrl requestFileDownload(
            @PathVariable("csvCollectionId") String csvCollectionId,
            @PathVariable("fileName") String fileName) {
        // Generate and send unique download URL
        String tokenParam = generateDownloadTokenParam(csvCollectionId);
        String filePath = "/" + fileName;
        String downloadLink = linkTo(CsvController.class,
                csvCollectionId).toString() + filePath + tokenParam;
        return new DownloadUrl(downloadLink);
    }

    @RequestMapping(value = "/{fileName:.+}", method = RequestMethod.GET)
    public void getFile(
            @PathVariable("csvCollectionId") String csvCollectionId,
            @PathVariable("fileName") String fileName,
            @RequestParam("token") String token,
            HttpServletResponse response) throws IOException {
        // Check validity of download token
        checkDownloadTokenValidity(token, csvCollectionId);
        // Send file
        File file = csvHandler.getFile(csvCollectionId, fileName);
        response.setContentLengthLong(file.length());
        response.setHeader("Content-disposition",
                "attachment;filename=" + fileName);
        try (InputStream fis = new FileInputStream(file)) {
            IOUtils.copyLarge(fis, response.getOutputStream());
            response.flushBuffer();
        } catch (FileNotFoundException ex) {
            throw new NotFoundException("File does not exist.", ex);
        }
    }

    private void checkDownloadTokenValidity(String token, String csvCollectionId) {
        Optional<DataDownloadToken> downloadToken = dataDownloadTokenRepository.findByToken(token);
        if (!downloadToken.isPresent() || !downloadToken.get().getDataId().equals(csvCollectionId)) {
            throw new ForbiddenException("Invalid download token.");
        }
    }

    private String generateDownloadTokenParam(String csvCollectionId) {
        // Check existence of CSV collection
        Optional<CsvCollection> coll = csvCollectionRepository.findById(
                csvCollectionId);
        if (!coll.isPresent()) {
            throw new ResourceNotFoundException(
                    "CSV collection " + csvCollectionId + " not found.");
        }

        // Generate download token
        DataDownloadToken downloadToken = new DataDownloadToken(csvCollectionId);
        dataDownloadTokenRepository.save(downloadToken);

        // Generate token param
        String tokenParam = "?token=" + downloadToken.getToken();

        return tokenParam;
    }


}
