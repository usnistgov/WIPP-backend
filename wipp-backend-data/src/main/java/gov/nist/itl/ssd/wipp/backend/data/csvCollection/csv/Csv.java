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

import com.fasterxml.jackson.annotation.JsonIgnore;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualRef;
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.CsvCollection;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Samia Benjida <samia.benjida at nist.gov>
 */
@Document
@CompoundIndexes({
        @CompoundIndex(
                name = "collection_filename",
                def = "{'csvCollection': 1, 'fileName': 1}",
                unique = true)
})
public class Csv {
    @Id
    @JsonIgnore
    private String id;

    @Indexed
    @ManualRef(CsvCollection.class)
    private String csvCollection;

    private String fileName;

    private String originalFileName;

    private long fileSize;

    private boolean importing;

    private String importError;

    public Csv() {
    }

    public Csv(String csvCollection, String fileName, String originalFileName, long fileSize,
                 boolean isImporting) {
        this(csvCollection, fileName, originalFileName, fileSize, isImporting, null);
    }

    public Csv(String csvCollection, String fileName, String originalFileName, long fileSize,
                 boolean isImporting, String importError) {
        this.csvCollection = csvCollection;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.importing = isImporting;
        this.importError = importError;
    }

    public String getId() {
        return id;
    }

    @JsonIgnore
    public String getCsvCollection() {
        return csvCollection;
    }

    public String getFileName() {
        return fileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isImporting() {
        return importing;
    }

    public String getImportError() {
        return importError;
    }

    public void setImporting(boolean importing) {
        this.importing = importing;
    }

    public void setImportError(String importError) {
        this.importError = importError;
    }

}
