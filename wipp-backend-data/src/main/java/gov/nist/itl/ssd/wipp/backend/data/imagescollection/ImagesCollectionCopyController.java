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
package gov.nist.itl.ssd.wipp.backend.data.imagescollection;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageHandler;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles.MetadataFileHandler;

import java.io.IOException;
import java.util.Optional;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Antoine Vandecreme
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Controller
@Tag(name="ImagesCollection Entity")
@RequestMapping(CoreConfig.BASE_URI + "/imagesCollections/{imagesCollectionId}/copy")
public class ImagesCollectionCopyController {

    @Autowired
    private ImagesCollectionRepository imagesCollectionRepository;

    @Autowired
    private ImageHandler imageRepository;

    @Autowired
    private MetadataFileHandler metadataFileRepository;

    @Autowired
    private ImagesCollectionLogic imagesCollectionLogic;

    @PreAuthorize("isAuthenticated() and "
    		+ "(hasRole('admin') or @imagesCollectionSecurity.checkAuthorize(#imagesCollectionId, false))")
    @RequestMapping(
            value = "",
            method = RequestMethod.POST)
    public ResponseEntity<ImagesCollection> copy(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            @RequestBody CopyRequestBody requestBody) {
        String name = requestBody.getName();
        if (name == null) {
            throw new ClientException(
                    "A name for the new collection must be provided.");
        }

        imagesCollectionLogic.assertCollectionNameUnique(name);

        Optional<ImagesCollection> tc = imagesCollectionRepository.findById(
                imagesCollectionId);
        if (!tc.isPresent()) {
            throw new ResourceNotFoundException(
                    "Images collection " + imagesCollectionId + " not found.");
        }

        ImagesCollection copy = new ImagesCollection(name);
        copy.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());
        copy = imagesCollectionRepository.save(copy);

        try {
            metadataFileRepository.copy(imagesCollectionId, copy.getId());
            String sourcePattern = requestBody.getSourcePattern();
            if (StringUtils.isEmpty(sourcePattern)) {
                imageRepository.copy(imagesCollectionId, copy.getId());
            } else {
                String destPattern = StringUtils.isEmpty(
                        requestBody.getDestinationPattern())
                                ? sourcePattern
                                : requestBody.getDestinationPattern();
                imageRepository.copy(imagesCollectionId, copy.getId(),
                        sourcePattern, destPattern);
            }
        } catch (IOException ex) {
            imagesCollectionRepository.delete(copy);
            throw new RuntimeException(
                    "Can not copy files to a new collection.", ex);
        }

        // Refresh with correct number of files and images
        copy = imagesCollectionRepository.findById(copy.getId()).get();
        return new ResponseEntity<>(copy, HttpStatus.CREATED);
    }

    public static class CopyRequestBody {

        private String name;

        private String sourcePattern;

        private String destinationPattern;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSourcePattern() {
            return sourcePattern;
        }

        public void setSourcePattern(String sourcePattern) {
            this.sourcePattern = sourcePattern;
        }

        public String getDestinationPattern() {
            return destinationPattern;
        }

        public void setDestinationPattern(String destinationPattern) {
            this.destinationPattern = destinationPattern;
        }
    }
}
