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
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataDownloadToken;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataDownloadTokenRepository;
import gov.nist.itl.ssd.wipp.backend.core.rest.DownloadUrl;
import gov.nist.itl.ssd.wipp.backend.core.rest.PaginationParameterTemplatesHelper;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollectionRepository;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@RestController
@Tag(name="ImagesCollection Entity")
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
    private DataDownloadTokenRepository dataDownloadTokenRepository;

    @Autowired
    private EntityLinks entityLinks;

    @Autowired
    private PaginationParameterTemplatesHelper paginationParameterTemplatesHelper;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @PreAuthorize("hasRole('admin') or @imagesCollectionSecurity.checkAuthorize(#imagesCollectionId, false)")
    public HttpEntity<PagedModel<EntityModel<Image>>> getFilesPage(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @ParameterObject @PageableDefault Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Image> assembler) {
        Page<Image> files = imageRepository.findByImagesCollection(
                imagesCollectionId, pageable);
        PagedModel<EntityModel<Image>> resources = assembler.toModel(files);

        resources.forEach(
                resource -> processResource(imagesCollectionId, resource));

        processCollectionResource(imagesCollectionId, resources, assembler);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @PreAuthorize("isAuthenticated() and "
    		+ "(hasRole('admin') or @imagesCollectionSecurity.checkAuthorize(#imagesCollectionId, true))")
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
    
    @RequestMapping(
            value = "/{fileName:.+}/request",
            method = RequestMethod.GET,
            produces = "application/json")
    @PreAuthorize("hasRole('admin') or @imagesCollectionSecurity.checkAuthorize(#imagesCollectionId, false)")
    public DownloadUrl requestImageDownload(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @PathVariable("fileName") String fileName) {
        // Generate and send unique download URL
        String tokenParam = generateDownloadTokenParam(imagesCollectionId);
        String imagePath = "/" + fileName;
        String downloadLink = linkTo(ImageController.class,
        		imagesCollectionId).toString() + imagePath + tokenParam;
        return new DownloadUrl(downloadLink);
    }

    @RequestMapping(value = "/{fileName:.+}", method = RequestMethod.HEAD)
    public void headFile(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @PathVariable("fileName") String fileName,
            @RequestParam("token") String token,
            HttpServletResponse response) throws IOException {
    	// Check validity of download token
    	checkDownloadTokenValidity(token, imagesCollectionId);
    	// Check existence of file and send length
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
            @RequestParam("token") String token,
            HttpServletResponse response) throws IOException {
    	// Check validity of download token
    	checkDownloadTokenValidity(token, imagesCollectionId);
    	// Send file
        File file = imageHandler.getFile(imagesCollectionId, fileName);
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

    @RequestMapping(value = "/{fileName:.+}", method = RequestMethod.DELETE)
    @PreAuthorize("isAuthenticated() and "
    		+ "(hasRole('admin') or @imagesCollectionSecurity.checkAuthorize(#imagesCollectionId, true))")
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
            produces = "application/json")
    @PreAuthorize("hasRole('admin') or @imagesCollectionSecurity.checkAuthorize(#imagesCollectionId, false)")
    public DownloadUrl requestOmeMetadataDownload(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @PathVariable("fileName") String fileName) {
        // Generate and send unique download URL
        String tokenParam = generateDownloadTokenParam(imagesCollectionId);
        String imageOmePath = "/" + fileName + "/ome/download";
        String downloadLink = linkTo(ImageController.class,
        		imagesCollectionId).toString() + imageOmePath + tokenParam;
        return new DownloadUrl(downloadLink);
    }

    @RequestMapping(
            value = "/{fileName:.+}/ome/download",
            method = RequestMethod.GET,
            produces = "text/xml;charset=UTF-8")
    public String getOmeMetadata(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @PathVariable("fileName") String fileName,
            @RequestParam("token") String token,
            HttpServletResponse response) throws IOException {
    	// Check validity of download token
    	checkDownloadTokenValidity(token, imagesCollectionId);
    	// Send file
        response.setHeader("Content-disposition",
                "attachment;filename=" + fileName + ".ome.xml");
        return imageHandler.getOmeXml(imagesCollectionId, fileName);
    }

    @RequestMapping(value = "/filterByFileNameRegex", method = RequestMethod.GET)
    @PreAuthorize("hasRole('admin') or @imagesCollectionSecurity.checkAuthorize(#imagesCollectionId, false)")
    public HttpEntity<PagedModel<EntityModel<Image>>> getFilesMatchingRegexPage(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @RequestParam(value="regex") String regex,
            @ParameterObject @PageableDefault Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Image> assembler) {
        Page<Image> files = imageRepository.findByImagesCollectionAndFileNameRegex(
                imagesCollectionId, regex, pageable);
        PagedModel<EntityModel<Image>> resources = assembler.toModel(files);
        resources.forEach(
                resource -> processResource(imagesCollectionId, resource));
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(value = "/filterByFileNameTimePattern", method = RequestMethod.GET)
    @PreAuthorize("hasRole('admin') or @imagesCollectionSecurity.checkAuthorize(#imagesCollectionId, false)")
    public HttpEntity<PagedModel<EntityModel<Image>>> getFilesMatchingTimePatternPage(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @RequestParam(value="timePattern") String timePattern,
            @ParameterObject @PageableDefault Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Image> assembler) {

        String tilePatternRegex = getRegexFromTimePattern(timePattern);
        Page<Image> files = imageRepository.findByImagesCollectionAndFileNameRegex(
                imagesCollectionId, tilePatternRegex, pageable);
        PagedModel<EntityModel<Image>> resources = assembler.toModel(files);
        resources.forEach(
                resource -> processResource(imagesCollectionId, resource));
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(value = "/getAllImagesList", method = RequestMethod.GET)
    @PreAuthorize("hasRole('admin') or @imagesCollectionSecurity.checkAuthorize(#imagesCollectionId, false)")
    public HttpEntity<List<Image>> getAllImagesList(
            @PathVariable("imagesCollectionId") String imagesCollectionId) {

        List<Image> files = imageRepository.findByImagesCollection(imagesCollectionId);
        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    protected void processResource(String imagesCollectionId,
            EntityModel<Image> resource) {
        Image file = resource.getContent();

        Link link = entityLinks.linkForItemResource(
                ImagesCollection.class, imagesCollectionId)
                .slash("images")
                .slash(file.getFileName())
                .withSelfRel();
        resource.add(link);
        
        link = entityLinks.linkForItemResource(
                ImagesCollection.class, imagesCollectionId)
                .slash("images")
                .slash(file.getFileName())
                .slash("request")
                .withRel("download");
        resource.add(link);

        link = entityLinks.linkForItemResource(
                ImagesCollection.class, imagesCollectionId)
                .slash("images")
                .slash(file.getFileName())
                .slash("ome")
                .withRel("ome");
        resource.add(link);

    }

    protected void processCollectionResource(String imagesCollectionId,
    		PagedModel<EntityModel<Image>> resources,
            PagedResourcesAssembler<Image> assembler) {

        // Add filterByFileNameRegex link to collection resource
    	Link imagesRegexFilterLink = entityLinks.linkForItemResource(
                ImagesCollection.class, imagesCollectionId)
                .slash("images")
                .slash("filterByFileNameRegex")
                .withRel("filterByFileNameRegex");

		TemplateVariable regexTv = new TemplateVariable("regex",
				TemplateVariable.VariableType.REQUEST_PARAM);

		UriTemplate regexFilterUriTemplate = UriTemplate.of(imagesRegexFilterLink.getHref(),
				new TemplateVariables(regexTv));

		Link regexFilterLink = Link.of(regexFilterUriTemplate, "filterByFileNameRegex");

		resources.add(paginationParameterTemplatesHelper.appendPaginationParameterTemplates(regexFilterLink));

        // Add filterByFileNameTimePattern link to collection resource
        Link imagesTimePatternLink = entityLinks.linkForItemResource(
                        ImagesCollection.class, imagesCollectionId)
                .slash("images")
                .slash("filterByFileNameTimePattern")
                .withRel("filterByFileNameTimePattern");

        TemplateVariable timePatternTv = new TemplateVariable("timePattern",
                TemplateVariable.VariableType.REQUEST_PARAM);

        UriTemplate timePatternFilterUriTemplate = UriTemplate.of(imagesTimePatternLink.getHref(),
                new TemplateVariables(timePatternTv));

        Link timePatternFilterLink = Link.of(timePatternFilterUriTemplate, "filterByFileNameTimePattern");

        resources.add(paginationParameterTemplatesHelper.appendPaginationParameterTemplates(timePatternFilterLink));

    }
    
    private void checkDownloadTokenValidity(String token, String imagesCollectionId) {
    	Optional<DataDownloadToken> downloadToken = dataDownloadTokenRepository.findByToken(token);
    	if (!downloadToken.isPresent() || !downloadToken.get().getDataId().equals(imagesCollectionId)) {
    		throw new ForbiddenException("Invalid download token.");
    	}
    }
    
    private String generateDownloadTokenParam(String imagesCollectionId) {
    	// Check existence of images collection
    	Optional<ImagesCollection> tc = imagesCollectionRepository.findById(
                imagesCollectionId);
        if (!tc.isPresent()) {
            throw new ResourceNotFoundException(
                    "Images collection " + imagesCollectionId + " not found.");
        }
        
        // Generate download token
        DataDownloadToken downloadToken = new DataDownloadToken(imagesCollectionId);
        dataDownloadTokenRepository.save(downloadToken);
        
        // Generate token param
        String tokenParam = "?token=" + downloadToken.getToken();
        
        return tokenParam;
    }

    // MIST time pattern to regex converter
    private String getRegexFromTimePattern(String filePattern) {
        Pattern pattern = Pattern.compile("(.*)(\\{[t]+\\})(.*)");
        Matcher matcher = pattern.matcher(filePattern);

        // Check if pattern is correct. We expect 3 groups: (*)({ttt})(*)
        if (!matcher.find() || matcher.groupCount() != 3) {
            throw new ClientException("Filename time pattern is invalid");
        }

        // The matcher should find at group: 0 - the entire string,
        // group 1 = prefix
        // group 2 = {i}
        // group 3 = suffix
        String prefix = matcher.group(1);
        int iCount = matcher.group(2).length() - 2;
        String suffix = matcher.group(3);

        return prefix + "\\d{" + iCount + "}" + suffix;
    }

}
