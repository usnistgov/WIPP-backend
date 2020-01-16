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
package gov.nist.itl.ssd.wipp.backend.data.imagescollection.images;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.PaginationParameterTemplatesHelper;
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

import org.apache.tomcat.util.http.fileupload.IOUtils;
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
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 */
@RestController
@Api(tags="ImagesCollection Entity")
@RequestMapping(CoreConfig.BASE_URI + "/imagesCollections/{imagesCollectionId}/images")
@ExposesResourceFor(Image.class)
public class ImageController {

    @Autowired
    private ImageHandler imageHandler;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImagesCollectionRepository imagesCollectionRepository;

    @Autowired
    private EntityLinks entityLinks;

    @Autowired
    private PaginationParameterTemplatesHelper paginationParameterTemplatesHelper;

    @PreAuthorize("@securityService.checkAuthorize(#imagesCollectionId)")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public HttpEntity<PagedResources<Resource<Image>>> getFilesPage(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @PageableDefault Pageable pageable,
            PagedResourcesAssembler<Image> assembler) {
        Page<Image> files = imageRepository.findByImagesCollection(
                imagesCollectionId, pageable);
        PagedResources<Resource<Image>> resources = assembler.toResource(files);

        resources.forEach(
                resource -> processResource(imagesCollectionId, resource));

        processCollectionResource(imagesCollectionId, resources, assembler);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public void deleteAllFiles(
            @PathVariable("imagesCollectionId") String imagesCollectionId) {
    	Optional<ImagesCollection> tc = imagesCollectionRepository.findById(
                imagesCollectionId);
        if (!tc.isPresent()) {
        	throw new NotFoundException("Collection not found");
        }
        if (tc.get().isLocked()) {
        	throw new ClientException("Collection locked.");
        }
        imageHandler.deleteAll(imagesCollectionId);
    }

    @RequestMapping(value = "/{fileName:.+}", method = RequestMethod.HEAD)
    public void headFile(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @PathVariable("fileName") String fileName,
            HttpServletResponse response) throws IOException {
        File file = imageHandler.getFile(imagesCollectionId, fileName);
        if (!file.exists()) {
            throw new NotFoundException("File does not exist.");
        }
        response.setContentLengthLong(file.length());
    }

    @RequestMapping(value = "/{fileName:.+}", method = RequestMethod.GET)
    public void getFile(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @PathVariable("fileName") String fileName,
            HttpServletResponse response) throws IOException {
        File file = imageHandler.getFile(imagesCollectionId, fileName);
        response.setContentLengthLong(file.length());
        try (InputStream fis = new FileInputStream(file)) {
            IOUtils.copyLarge(fis, response.getOutputStream());
            response.flushBuffer();
        } catch (FileNotFoundException ex) {
            throw new NotFoundException("File does not exist.", ex);
        }
    }

    @RequestMapping(value = "/{fileName:.+}", method = RequestMethod.DELETE)
    public void deleteFile(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @PathVariable("fileName") String fileName) {
    	Optional<ImagesCollection> tc = imagesCollectionRepository.findById(
                imagesCollectionId);
        if (!tc.isPresent()) {
        	throw new NotFoundException("Collection not found");
        }
        if (tc.get().isLocked()) {
        	throw new ClientException("Collection locked.");
        }
        imageHandler.delete(imagesCollectionId, fileName);
    }

    @RequestMapping(
            value = "/{fileName:.+}/ome",
            method = RequestMethod.GET,
            produces = "text/xml;charset=UTF-8")
    public String getOmeMetadata(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @PathVariable("fileName") String fileName,
            HttpServletResponse response) throws IOException {
        response.setHeader("Content-disposition",
                "attachment;filename=" + fileName + ".ome.xml");
        return imageHandler.getOmeXml(imagesCollectionId, fileName);
    }

    @RequestMapping(value = "filterByFileNameRegex", method = RequestMethod.GET)
    public HttpEntity<PagedResources<Resource<Image>>> getFilesMatchingRegexPage(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @RequestParam(value="regex") String regex,
            @PageableDefault Pageable pageable,
            PagedResourcesAssembler<Image> assembler) {
        Page<Image> files = imageRepository.findByImagesCollectionAndFileNameRegex(
                imagesCollectionId, regex, pageable);
        PagedResources<Resource<Image>> resources = assembler.toResource(files);
        resources.forEach(
                resource -> processResource(imagesCollectionId, resource));
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    protected void processResource(String imagesCollectionId,
            Resource<Image> resource) {
        Image file = resource.getContent();

        Link link = entityLinks.linkForSingleResource(
                ImagesCollection.class, imagesCollectionId)
                .slash("images")
                .slash(file.getFileName())
                .withSelfRel();
        resource.add(link);

        link = entityLinks.linkForSingleResource(
                ImagesCollection.class, imagesCollectionId)
                .slash("images")
                .slash(file.getFileName())
                .slash("ome")
                .withRel("ome");
        resource.add(link);

    }

    protected void processCollectionResource(String imagesCollectionId,
    		PagedResources<Resource<Image>> resources,
            PagedResourcesAssembler<Image> assembler) {

    	Link imagesFilterLink = entityLinks.linkForSingleResource(
                ImagesCollection.class, imagesCollectionId)
                .slash("images")
                .slash("filterByFileNameRegex")
                .withRel("filterByFileNameRegex");

		TemplateVariable tv = new TemplateVariable("regex",
				TemplateVariable.VariableType.REQUEST_PARAM);

		UriTemplate uriTemplate = new UriTemplate(imagesFilterLink.getHref(),
				new TemplateVariables(tv));

		Link link = new Link(uriTemplate, "filterByFileNameRegex");

		resources.add(paginationParameterTemplatesHelper.appendPaginationParameterTemplates(link));

    }
}
