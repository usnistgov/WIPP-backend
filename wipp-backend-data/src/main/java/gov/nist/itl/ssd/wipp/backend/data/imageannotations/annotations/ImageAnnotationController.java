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
package gov.nist.itl.ssd.wipp.backend.data.imageannotations.annotations;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.imageannotations.ImageAnnotationsCollection;
import gov.nist.itl.ssd.wipp.backend.data.imageannotations.ImageAnnotationsCollectionRepository;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@RestController
@Tag(name="ImageAnnotations Entity")
@RequestMapping(CoreConfig.BASE_URI + "/imageAnnotationsCollections/{imageAnnotationsCollectionId}/annotations")
@ExposesResourceFor(ImageAnnotation.class)
public class ImageAnnotationController {

    @Autowired
    private CoreConfig config;

    @Autowired
    private EntityLinks entityLinks;

    @Autowired
    private ImageAnnotationRepository imageAnnotationRepository;

    @Autowired
    private ImageAnnotationsCollectionRepository imageAnnotationsCollectionRepository;

    @Autowired
    private ImageAnnotationHandler imageAnnotationHandler;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @PreAuthorize("hasRole('admin') or @imageAnnotationsCollectionSecurity.checkAuthorize(#imageAnnotationsCollectionId, false)")
    public HttpEntity<PagedModel<EntityModel<ImageAnnotation>>> getFilesPage(
            @PathVariable("imageAnnotationsCollectionId") String imageAnnotationsCollectionId,
            @ParameterObject @PageableDefault Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<ImageAnnotation> assembler) {
        Page<ImageAnnotation> files = imageAnnotationRepository.findByImageAnnotationsCollection(
                imageAnnotationsCollectionId, pageable);
        PagedModel<EntityModel<ImageAnnotation>> resources
                = assembler.toModel(files);
        resources.forEach(
                resource -> processResource(imageAnnotationsCollectionId, resource));
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @PreAuthorize("hasRole('admin') or @imageAnnotationsCollectionSecurity.checkAuthorize(#imageAnnotationsCollectionId, false)")
    public HttpEntity<PagedModel<EntityModel<ImageAnnotation>>> addAnnotations(
            @PathVariable("imageAnnotationsCollectionId") String imageAnnotationsCollectionId,
            @ParameterObject @PageableDefault Pageable pageable,
            @RequestBody List<ImageAnnotation> annotations,
            @Parameter(hidden = true) PagedResourcesAssembler<ImageAnnotation> assembler) {
        annotations.forEach(annotation -> {
            imageAnnotationRepository.save(annotation);
        });
        Page<ImageAnnotation> files = imageAnnotationRepository.findByImageAnnotationsCollection(
                imageAnnotationsCollectionId, pageable);
        PagedModel<EntityModel<ImageAnnotation>> resources
                = assembler.toModel(files);
        resources.forEach(
                resource -> processResource(imageAnnotationsCollectionId, resource));
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @PreAuthorize("isAuthenticated() and "
    		+ "(hasRole('admin') or @imageAnnotationsCollectionSecurity.checkAuthorize(#imageAnnotationsCollectionId, true))")
    public void deleteAllFiles(
            @PathVariable("imageAnnotationsCollectionId") String imageAnnotationsCollectionId) {
        Optional<ImageAnnotationsCollection> tc = imageAnnotationsCollectionRepository.findById(
                imageAnnotationsCollectionId);
        if (!tc.isPresent()) {
            throw new NotFoundException("Collection not found");
        }
        imageAnnotationHandler.deleteAll(imageAnnotationsCollectionId);
    }

    @RequestMapping(value = "/{imageFileName:.+}", method = RequestMethod.DELETE)
    @PreAuthorize("isAuthenticated() and "
    		+ "(hasRole('admin') or @imageAnnotationsCollectionSecurity.checkAuthorize(#imageAnnotationsCollectionId, true))")
    public void deleteFile(
            @PathVariable("imageAnnotationsCollectionId") String imageAnnotationsCollectionId,
            @PathVariable("imageFileName") String imageFileName) {
        Optional<ImageAnnotationsCollection> tc = imageAnnotationsCollectionRepository.findById(
                imageAnnotationsCollectionId);
        if (!tc.isPresent()) {
            throw new NotFoundException("Collection not found");
        }
        imageAnnotationHandler.delete(imageAnnotationsCollectionId, imageFileName);
    }

    @RequestMapping(value = "/{fileName:.+}", method = RequestMethod.HEAD)
    @PreAuthorize("hasRole('admin') or @imageAnnotationsCollectionSecurity.checkAuthorize(#imageAnnotationsCollectionId, false)")
    public void headFile(
            @PathVariable("imageAnnotationsCollectionId") String imageAnnotationsCollectionId,
            @PathVariable("fileName") String fileName,
            HttpServletResponse response) throws IOException {
        File file = this.getFile(imageAnnotationsCollectionId, fileName);
        if (!file.exists()) {
            throw new NotFoundException("File does not exist.");
        }
        response.setContentLengthLong(file.length());
    }
    @RequestMapping(value = "/{fileName:.+}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('admin') or @imageAnnotationsCollectionSecurity.checkAuthorize(#imageAnnotationsCollectionId, false)")
    public void getFile(
            @PathVariable("imageAnnotationsCollectionId") String imageAnnotationsCollectionId,
            @PathVariable("fileName") String fileName,
            HttpServletResponse response) throws IOException {
        File file = this.getFile(imageAnnotationsCollectionId, fileName);
        response.setContentLengthLong(file.length());
        try (InputStream fis = new FileInputStream(file)) {
            IOUtils.copyLarge(fis, response.getOutputStream());
            response.flushBuffer();
        } catch (FileNotFoundException ex) {
            throw new NotFoundException("File does not exist.", ex);
        }
    }

    public File getFile(String imageAnnotationsCollectionId, String fileName) {
        return new File(new File(config.getImageAnnotationsFolder(), imageAnnotationsCollectionId), fileName);
    }

    protected void processResource(String imageAnnotationsCollectionId,
                                   EntityModel<ImageAnnotation> resource) {
        ImageAnnotation file = resource.getContent();
        Link link = entityLinks.linkForItemResource(
                ImageAnnotationsCollection.class, imageAnnotationsCollectionId)
                .slash("annotations")
                .slash(file.getImageFileName())
                .withSelfRel();
        resource.add(link);
    }

}
