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
package gov.nist.itl.ssd.wipp.backend.data.genericdata.genericfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.data.genericdata.GenericData;
import gov.nist.itl.ssd.wipp.backend.data.genericdata.GenericDataRepository;
import gov.nist.itl.ssd.wipp.backend.data.utils.flowjs.FlowFile;
import gov.nist.itl.ssd.wipp.backend.data.utils.flowjs.FlowjsController;
import io.swagger.annotations.Api;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi@nist.gov>
*/
@RestController
@Api(tags="GenericData Entity")
@RequestMapping(CoreConfig.BASE_URI + "/genericDatas/{genericDataId}/genericFile")
public class GenericFileUploadController  extends FlowjsController {
	

    private static final Logger LOG = Logger.getLogger(GenericFileUploadController.class.getName());

    @Autowired
    private GenericFileRepository genericFileRepository;

    @Autowired
    private GenericDataRepository genericDataRepository;

    @Autowired
    private CoreConfig config;

    @RequestMapping(
            value = "",
            method = RequestMethod.GET,
            params = {
                    "flowChunkNumber", "flowTotalChunks", "flowChunkSize",
                    "flowTotalSize", "flowIdentifier", "flowFilename",
                    "flowRelativePath"})
    public void isChunckUploaded(
            @PathVariable("genericDataId") String genericDataId,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        assertCollectionModifiable(genericDataId);
        super.isChunckUploaded(request, response,
                new GenericDataFileParameters(genericDataId));
    }

    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            params = {
                    "flowChunkNumber", "flowTotalChunks", "flowChunkSize",
                    "flowTotalSize", "flowIdentifier", "flowFilename",
                    "flowRelativePath"})
    public void uploadChunck(
            @PathVariable("genericDataId") String genericDataId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        assertCollectionModifiable(genericDataId);
        super.uploadChunck(request, response,
                new GenericDataFileParameters(genericDataId));
    }

    private void assertCollectionModifiable(String genericDataId) {
        Optional<GenericData> oldTc = genericDataRepository.findById(
        		genericDataId);
        if (! oldTc.isPresent()) {
            throw new ClientException("Collection " + genericDataId
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
        return new GenericDataFlowFile(flowFile,
                (GenericDataFileParameters) parameters);
    }

    @Override
    protected File getUploadDir(FlowFile flowFile) {
        return getUploadDir(getCollectionId(flowFile));
    }

    @Override
    protected String getUploadSubFolder() {
        return null;
    }

    protected File getUploadDir(String genericDataId) {
        return new File(config.getGenericDatasFolder(), genericDataId);
    }

    @Override
    protected File getTempUploadDir(FlowFile flowFile) {
        return getTempUploadDir(getCollectionId(flowFile));
    }

    protected File getTempUploadDir(String genericDataId) {
        return new File(config.getGenericDatasUploadTmpFolder(), genericDataId);
    }

    @Override
    protected void onUploadFinished(FlowFile flowFile, Path tempPath)
            throws IOException {
        String collectionId = getCollectionId(flowFile);
        String fileName = flowFile.getFlowFilename();

        try {
            GenericData genericData = genericDataRepository.findById(collectionId).get();
        } catch (NoSuchElementException e) {
            LOG.log(Level.WARNING, "Error finding collection " + collectionId
                            + " when uploading file " + fileName,
                    e);
        }

        if(fileName != null){
            fileName = fileName.replaceAll("[\\p{Punct}&&[^.-]]", "_");
            fileName = fileName.replace(" ", "");
            uploadGenericFile(flowFile, tempPath, fileName);
        }
    }

    private void uploadGenericFile(FlowFile flowFile, Path tempPath, String fileName) throws IOException{
        File uploadDir = getUploadDir(flowFile);
        uploadDir.mkdirs();
        String collectionId = getCollectionId(flowFile);
        GenericFile genericFile  = new GenericFile(collectionId, fileName, flowFile.getFlowFilename(),
                getPathSize(tempPath), true);
        genericFileRepository.save(genericFile);
        genericDataRepository.updateGenericFileCaches(collectionId);
        Path outputPath = new File(uploadDir, fileName).toPath();
        Files.copy(tempPath, outputPath);
        Files.delete(tempPath);
        genericFile.setFileName(fileName);
        genericFile.setFileSize(getPathSize(outputPath));
        genericFile.setImporting(false);
        genericFileRepository.save(genericFile);
        genericDataRepository.updateGenericFileCaches(collectionId);
    }

    protected static String getCollectionId(FlowFile flowFile) {
        GenericDataFlowFile tff = (GenericDataFlowFile) flowFile;
        return tff.collection;
    }

    private static class GenericDataFileParameters implements Parameters {

        private final String collectionId;

        public GenericDataFileParameters(String collectionId) {
            this.collectionId = collectionId;
        }
    }

    private static class GenericDataFlowFile extends FlowFile {

        private final String collection;

        public GenericDataFlowFile(FlowFile flowFile,
        			GenericDataFileParameters parameters) {
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
            final GenericDataFlowFile other = (GenericDataFlowFile) obj;
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
