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
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.Image;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageHandler;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageRepository;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles.MetadataFile;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles.MetadataFileHandler;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles.MetadataFileRepository;
import io.swagger.annotations.Api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 */
@Controller
@Api(tags="ImagesCollection Entity")
@RequestMapping(CoreConfig.BASE_URI + "/imagesCollections/{imagesCollectionId}/download")
public class ImagesCollectionDownloadController {

    @Autowired
    private ImagesCollectionRepository imagesCollectionRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageHandler imageHandler;

    @Autowired
    private MetadataFileRepository metadataFileRepository;

    @Autowired
    private MetadataFileHandler metadataFileHandler;

    @RequestMapping(
            value = "",
            method = RequestMethod.GET,
            produces = "application/zip")
    // We make sure the user trying to download the collection has the right to access it
    @PreAuthorize("@securityServiceData.checkAuthorizeImagesCollectionId(#imagesCollectionId)")
    public void get(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            HttpServletResponse response) throws IOException {
    	Optional<ImagesCollection> tc = imagesCollectionRepository.findById(
                imagesCollectionId);
        if (!tc.isPresent()) {
            throw new ResourceNotFoundException(
                    "Images collection " + imagesCollectionId + " not found.");
        }

        response.setHeader("Content-disposition",
                "attachment;filename=" + tc.get().getName() + ".zip");

        ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
        List<Image> images = imageRepository.findByImagesCollection(
                imagesCollectionId);
        for (Image image : images) {
            zos.putNextEntry(new ZipEntry("images/" + image.getFileName()));
            try (InputStream is = imageHandler.getInputStream(
                    imagesCollectionId, image.getFileName())) {
                IOUtils.copyLarge(is, zos);
            }
        }

        List<MetadataFile> metadataFiles = metadataFileRepository
                .findByImagesCollection(imagesCollectionId);
        for (MetadataFile metadataFile : metadataFiles) {
            zos.putNextEntry(new ZipEntry(
                    "metadata/" + metadataFile.getFileName()));
            try (InputStream is = metadataFileHandler.getInputStream(
                    imagesCollectionId, metadataFile.getFileName())) {
                IOUtils.copyLarge(is, zos);
            }
        }
        zos.finish();
    }

}
