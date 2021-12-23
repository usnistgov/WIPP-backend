package gov.nist.itl.ssd.wipp.backend.data.genericdata.genericfiles;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.genericdata.GenericData;
import gov.nist.itl.ssd.wipp.backend.data.genericdata.GenericDataRepository;

import io.swagger.annotations.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
*/
@RestController
@Api(tags="GenericData Entity")
@RequestMapping(CoreConfig.BASE_URI + "/genericDatas/{genericDataId}/genericFile")
@ExposesResourceFor(GenericFile.class)
public class GenericFileController {
	
	@Autowired
    private EntityLinks entityLinks;

    @Autowired
    private GenericFileRepository genericFileRepository;

    @Autowired
    private GenericDataRepository genericDataRepository;

    @Autowired
    private GenericFileHandler genericFileHandler;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @PreAuthorize("hasRole('admin') or @genericDataSecurity.checkAuthorize(#genericDataId, false)")
    public HttpEntity<PagedModel<EntityModel<GenericFile>>> getFilesPage(
            @PathVariable("genericDataId") String genericDataId,
            @PageableDefault Pageable pageable,
            PagedResourcesAssembler<GenericFile> assembler) {
        Page<GenericFile> files = genericFileRepository.findByGenericData(
        		genericDataId, pageable);
        PagedModel<EntityModel<GenericFile>> resources
                = assembler.toModel(files);
        resources.forEach(
                resource -> processResource(genericDataId, resource));
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @PreAuthorize("isAuthenticated() and "
    		+ "(hasRole('admin') or @genericDataSecurity.checkAuthorize(#genericDataId, true))")
    public void deleteAllFiles(
            @PathVariable("genericDataId") String genericDataId) {
        Optional<GenericData> tc = genericDataRepository.findById(
        		genericDataId);
        if (!tc.isPresent()) {
            throw new NotFoundException("Collection not found");
        }
        if (tc.get().isLocked()) {
            throw new ClientException("Collection locked.");
        }
        genericFileHandler.deleteAll(genericDataId);
    }

    @RequestMapping(value = "/{fileName:.+}", method = RequestMethod.DELETE)
    @PreAuthorize("isAuthenticated() and "
    		+ "(hasRole('admin') or @genericDataSecurity.checkAuthorize(#genericDataId, true))")
    public void deleteFile(
            @PathVariable("genericDataId") String genericDataId,
            @PathVariable("fileName") String fileName) {
        Optional<GenericData> tc = genericDataRepository.findById(
        		genericDataId);
        if (!tc.isPresent()) {
            throw new NotFoundException("Collection not found");
        }
        if (tc.get().isLocked()) {
            throw new ClientException("Collection locked.");
        }
        genericFileHandler.delete(genericDataId, fileName);
    }

    protected void processResource(String genericDataId,
                                   EntityModel<GenericFile> resource) {
        GenericFile file = resource.getContent();
        Link link = entityLinks.linkForItemResource(
                GenericData.class, genericDataId)
                .slash("genericFile")
                .slash(file.getFileName())
                .withSelfRel();
        resource.add(link);
    }

}
