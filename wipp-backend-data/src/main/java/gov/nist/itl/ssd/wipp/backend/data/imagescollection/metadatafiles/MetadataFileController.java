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
package gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollectionRepository;
import io.swagger.annotations.Api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 */
@RestController
@Api(tags="ImagesCollection Entity")
@RequestMapping(CoreConfig.BASE_URI + "/imagesCollections/{imagesCollectionId}/metadataFiles")
@ExposesResourceFor(MetadataFile.class)
public class MetadataFileController {

    @Autowired
    private MetadataFileHandler metadataFileHandler;

    @Autowired
    private MetadataFileRepository metadataFileRepository;

    @Autowired
    private ImagesCollectionRepository imagesCollectionRepository;

    @Autowired
    private EntityLinks entityLinks;

    @RequestMapping(value = "", method = RequestMethod.GET)
    // We make sure the user trying to call the getFilesPage method is authorized to access the image collection
    @PreAuthorize("@securityServiceData.checkAuthorizeImagesCollectionId(#imagesCollectionId)")
    public HttpEntity<PagedResources<Resource<MetadataFile>>> getFilesPage(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @PageableDefault Pageable pageable,
            PagedResourcesAssembler<MetadataFile> assembler) {
        Page<MetadataFile> files = metadataFileRepository.findByImagesCollection(
                imagesCollectionId, pageable);
        PagedResources<Resource<MetadataFile>> resources
                = assembler.toResource(files);
        resources.forEach(
                resource -> processResource(imagesCollectionId, resource));
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    // We make sure the user trying to call the deleteAllFiles method is logged in and authorized to access the image collection
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizeImagesCollectionId(#imagesCollectionId)")
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public void deleteAllFiles(
            @PathVariable("imagesCollectionId") String imagesCollectionId) {
    	Optional<ImagesCollection> tc = imagesCollectionRepository.findById(
                imagesCollectionId);
    	if (!tc.isPresent()) {
        	throw new NotFoundException("Image collection does not exist.");
        }
        if (tc.get().isLocked()) {
            throw new ClientException("Collection locked.");
        }
        metadataFileHandler.deleteAll(imagesCollectionId);
    }

    // We make sure the user trying to call the headFile method is authorized to access the image collection
    @PreAuthorize("@securityServiceData.checkAuthorizeImagesCollectionId(#imagesCollectionId)")
    @RequestMapping(value = "/{fileName:.+}", method = RequestMethod.HEAD)
    public void headFile(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @PathVariable("fileName") String fileName,
            HttpServletResponse response) throws IOException {
        File file = metadataFileHandler.getFile(imagesCollectionId, fileName);
        if (!file.exists()) {
            throw new NotFoundException("File does not exist.");
        }
        response.setContentLengthLong(file.length());
    }

    // We make sure the user trying to call the getFile method is authorized to access the image collection
    @PreAuthorize("@securityServiceData.checkAuthorizeImagesCollectionId(#imagesCollectionId)")
    @RequestMapping(value = "/{fileName:.+}", method = RequestMethod.GET)
    public void getFile(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @PathVariable("fileName") String fileName,
            HttpServletResponse response) throws IOException {
        File file = metadataFileHandler.getFile(imagesCollectionId, fileName);

        response.setContentLengthLong(file.length());
        try (InputStream fis = new FileInputStream(file)) {
            IOUtils.copyLarge(fis, response.getOutputStream());
            response.flushBuffer();
        } catch (FileNotFoundException ex) {
            throw new NotFoundException("File does not exist.", ex);
        }
    }

    // We make sure the user trying to call the deleteFile method is logged in and authorized to access the image collection
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizeImagesCollectionId(#imagesCollectionId)")
    @RequestMapping(value = "/{fileName:.+}", method = RequestMethod.DELETE)
    public void deleteFile(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @PathVariable("fileName") String fileName) {
    	Optional<ImagesCollection> tc = imagesCollectionRepository.findById(
                imagesCollectionId);
    	if (!tc.isPresent()) {
        	throw new NotFoundException("Image collection does not exist.");
        }
        if (tc.get().isLocked()) {
            throw new ClientException("Collection locked.");
        }
        metadataFileHandler.delete(imagesCollectionId, fileName);
    }

    protected void processResource(String imagesCollectionId,
            Resource<MetadataFile> resource) {
        MetadataFile file = resource.getContent();

        Link link = entityLinks.linkForSingleResource(
                ImagesCollection.class, imagesCollectionId)
                .slash("metadataFiles")
                .slash(file.getFileName())
                .withSelfRel();
        resource.add(link);
    }
}
