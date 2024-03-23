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
package gov.nist.itl.ssd.wipp.backend.data.imagescollection.files;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.data.utils.flowjs.FlowFile;
import gov.nist.itl.ssd.wipp.backend.data.utils.flowjs.FlowjsController;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollectionRepository;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
public abstract class FileUploadController extends FlowjsController {

    @Autowired
    private CoreConfig config;

    @Autowired
    private ImagesCollectionRepository imagesCollectionRepository;

    @RequestMapping(
            value = "",
            method = RequestMethod.GET,
            params = {
                "flowChunkNumber", "flowTotalChunks", "flowChunkSize",
                "flowTotalSize", "flowIdentifier", "flowFilename",
                "flowRelativePath"})
    @PreAuthorize("isAuthenticated() and "
    		+ "(hasRole('admin') or @imagesCollectionSecurity.checkAuthorize(#imagesCollectionId, true))")
    public void isChunckUploaded(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        assertCollectionModifiable(imagesCollectionId);
        super.isChunckUploaded(request, response,
                new ImagesCollectionFileParameters(imagesCollectionId));
    }

    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            params = {
                "flowChunkNumber", "flowTotalChunks", "flowChunkSize",
                "flowTotalSize", "flowIdentifier", "flowFilename",
                "flowRelativePath"})
    @PreAuthorize("isAuthenticated() and "
    		+ "(hasRole('admin') or @imagesCollectionSecurity.checkAuthorize(#imagesCollectionId, true))")
    public void uploadChunck(
            @PathVariable("imagesCollectionId") String imagesCollectionId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        assertCollectionModifiable(imagesCollectionId);
        super.uploadChunck(request, response,
                new ImagesCollectionFileParameters(imagesCollectionId));
    }

    private void assertCollectionModifiable(String imagesCollectionId) {
    	Optional<ImagesCollection> oldTc = imagesCollectionRepository.findById(
                imagesCollectionId);
        if (! oldTc.isPresent()) {
            throw new ClientException("Collection " + imagesCollectionId
                    + " does not exist.");
        }
        if (oldTc.get().isLocked()) {
            throw new ClientException("Collection locked.");
        }
    }

    @Override
    protected FlowFile getFlowFile(HttpServletRequest request,
            Parameters parameters) {
        FlowFile flowFile = super.getFlowFile(request, parameters);
        return new ImagesCollectionFlowFile(flowFile,
                (ImagesCollectionFileParameters) parameters);
    }

    @Override
    protected File getUploadDir(FlowFile flowFile) {
        return getUploadDir(getCollectionId(flowFile));
    }

    @Override
    protected File getTempUploadDir(FlowFile flowFile) {
        return getTempUploadDir(getCollectionId(flowFile));
    }

    protected static String getCollectionId(FlowFile flowFile) {
        ImagesCollectionFlowFile tff = (ImagesCollectionFlowFile) flowFile;
        return tff.collection;
    }

    private static class ImagesCollectionFileParameters implements Parameters {

        private final String collectionId;

        public ImagesCollectionFileParameters(String collectionId) {
            this.collectionId = collectionId;
        }
    }

    private static class ImagesCollectionFlowFile extends FlowFile {

        private final String collection;

        public ImagesCollectionFlowFile(FlowFile flowFile,
                ImagesCollectionFileParameters parameters) {
            super(flowFile);
            this.collection = parameters.collectionId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            if (!super.equals(obj)) {
                return false;
            }
            final ImagesCollectionFlowFile other = (ImagesCollectionFlowFile) obj;
            return Objects.equals(this.collection, other.collection);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 67 * hash + Objects.hashCode(this.collection);
            return hash;
        }
    }
}
