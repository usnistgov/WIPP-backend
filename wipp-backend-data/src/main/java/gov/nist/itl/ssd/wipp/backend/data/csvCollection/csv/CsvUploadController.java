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
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.CsvCollection;
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.CsvCollectionRepository;
import gov.nist.itl.ssd.wipp.backend.data.utils.flowjs.FlowFile;
import io.swagger.annotations.Api;
import gov.nist.itl.ssd.wipp.backend.data.utils.flowjs.FlowjsController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Samia Benjida <samia.benjida at nist.gov>
 */
@RestController
@Api(tags="CsvCollection Entity")
@RequestMapping(CoreConfig.BASE_URI + "/csvCollections/{csvCollectionId}/csv")
public class CsvUploadController  extends FlowjsController {

    private static final Logger LOG = Logger.getLogger(CsvUploadController.class.getName());

    @Autowired
    private CsvRepository csvRepository;

    @Autowired
    private CsvCollectionRepository csvCollectionRepository;

    @Autowired
    private CoreConfig config;

    @RequestMapping(
            value = "",
            method = RequestMethod.GET,
            params = {
                    "flowChunkNumber", "flowTotalChunks", "flowChunkSize",
                    "flowTotalSize", "flowIdentifier", "flowFilename",
                    "flowRelativePath"})
    // We make sure that the user is logged in and has the right to access the csv collection before accessing the isChunckUploaded method
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizeCsvCollectionId(#csvCollectionId, false)")
    public void isChunckUploaded(
            @PathVariable("csvCollectionId") String csvCollectionId,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        assertCollectionModifiable(csvCollectionId);
        super.isChunckUploaded(request, response,
                new CsvCollectionFileParameters(csvCollectionId));
    }

    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            params = {
                    "flowChunkNumber", "flowTotalChunks", "flowChunkSize",
                    "flowTotalSize", "flowIdentifier", "flowFilename",
                    "flowRelativePath"})
    // We make sure that the user is logged in and has the right to access the csv collection before accessing the uploadChunck method
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizeCsvCollectionId(#csvCollectionId, true)")
    public void uploadChunck(
            @PathVariable("csvCollectionId") String csvCollectionId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        assertCollectionModifiable(csvCollectionId);
        super.uploadChunck(request, response,
                new CsvCollectionFileParameters(csvCollectionId));
    }

    private void assertCollectionModifiable(String csvCollectionId) {
        Optional<CsvCollection> oldTc = csvCollectionRepository.findById(
                csvCollectionId);
        if (! oldTc.isPresent()) {
            throw new ClientException("Collection " + csvCollectionId
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
        return new CsvCollectionFlowFile(flowFile,
                (CsvCollectionFileParameters) parameters);
    }

    @Override
    protected File getUploadDir(FlowFile flowFile) {
        return getUploadDir(getCollectionId(flowFile));
    }

    @Override
    protected String getUploadSubFolder() {
        return null;
    }

    protected File getUploadDir(String csvCollectionId) {
        return new File(config.getCsvCollectionsFolder(), csvCollectionId);
    }

    @Override
    protected File getTempUploadDir(FlowFile flowFile) {
        return getTempUploadDir(getCollectionId(flowFile));
    }

    protected File getTempUploadDir(String csvCollectionId) {
        return new File(config.getCsvCollectionsUploadTmpFolder(), csvCollectionId);
    }

    @Override
    protected void onUploadFinished(FlowFile flowFile, Path tempPath)
            throws IOException {
        String collectionId = getCollectionId(flowFile);
        String fileName = flowFile.getFlowFilename();

        try {
            CsvCollection csvCol = csvCollectionRepository.findById(collectionId).get();
        } catch (NoSuchElementException e) {
            LOG.log(Level.WARNING, "Error finding collection " + collectionId
                            + " when uploading file " + fileName,
                    e);
        }

        if(fileName != null){
            fileName = fileName.replaceAll("[\\p{Punct}&&[^.-]]", "_");
            fileName = fileName.replace(" ", "");
            uploadCsv(flowFile, tempPath, fileName);
        }
    }

    private void uploadCsv(FlowFile flowFile, Path tempPath, String fileName) throws IOException{
        File uploadDir = getUploadDir(flowFile);
        uploadDir.mkdirs();
        String collectionId = getCollectionId(flowFile);
        Csv csv = new Csv(collectionId, fileName, flowFile.getFlowFilename(),
                getPathSize(tempPath), true);
        csvRepository.save(csv);
        csvCollectionRepository.updateCsvCaches(collectionId);
        Path outputPath = new File(uploadDir, fileName).toPath();
        Files.copy(tempPath, outputPath);
        Files.delete(tempPath);
        csv.setFileName(fileName);
        csv.setFileSize(getPathSize(outputPath));
        csv.setImporting(false);
        csvRepository.save(csv);
        csvCollectionRepository.updateCsvCaches(collectionId);
    }

    protected static String getCollectionId(FlowFile flowFile) {
        CsvCollectionFlowFile tff = (CsvCollectionFlowFile) flowFile;
        return tff.collection;
    }

    private static class CsvCollectionFileParameters implements Parameters {

        private final String collectionId;

        public CsvCollectionFileParameters(String collectionId) {
            this.collectionId = collectionId;
        }
    }

    private static class CsvCollectionFlowFile extends FlowFile {

        private final String collection;

        public CsvCollectionFlowFile(FlowFile flowFile,
                                     CsvCollectionFileParameters parameters) {
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
            final CsvCollectionFlowFile other = (CsvCollectionFlowFile) obj;
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
