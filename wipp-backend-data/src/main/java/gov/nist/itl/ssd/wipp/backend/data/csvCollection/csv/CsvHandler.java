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
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.CsvCollectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Samia Benjida <samia.benjida at nist.gov>
 */
@Component
public class CsvHandler {

    @Autowired
    private CsvRepository csvRepository;

    @Autowired
    private CsvCollectionRepository csvCollectionRepository;

    @Autowired
    private CoreConfig config;

    protected void addAllInDb(String csvCollectionId) {
        File[] files = getFiles(csvCollectionId);
        if (files == null) {
            return;
        }

        List<Csv> csvFiles = Arrays.stream(files).map(
                f -> new Csv(
                        csvCollectionId, f.getName(), f.getName(), getFileSize(f), true))
                .collect(Collectors.toList());

        csvRepository.saveAll(csvFiles);
        csvCollectionRepository.updateCsvCaches(csvCollectionId);
    }

    protected void deleteAllInDb(String csvCollectionId) {
        csvRepository.deleteByCsvCollection(csvCollectionId);
        csvCollectionRepository.updateCsvCaches(csvCollectionId);
    }

    protected void deleteInDb(String csvCollectionId, String fileName) {
        csvRepository.deleteByCsvCollectionAndFileName(
                csvCollectionId, fileName);
        csvCollectionRepository.updateCsvCaches(csvCollectionId);
    }

    public void importFolder(String csvCollectionId, File folder)
            throws IOException {
        File csvFilesFolder = getFilesFolder(csvCollectionId);
        csvFilesFolder.getParentFile().mkdirs();
        Files.move(folder.toPath(), csvFilesFolder.toPath());
        addAllInDb(csvCollectionId);
    }

    public File getFile(String csvCollectionId, String fileName) {
        return new File(getFilesFolder(csvCollectionId), fileName);
    }

    protected File[] getFiles(String csvCollectionId) {
        return getFilesFolder(csvCollectionId).listFiles(File::isFile);
    }

    public File getFilesFolder(String csvCollectionId) {
        return new File(config.getCsvCollectionsFolder(), csvCollectionId);
    }

    protected static long getFileSize(File file) {
        long size = 0;
        try {
            size = Files.size(file.toPath());
        } catch (IOException O_o) {
            // Safely ignore, the size will just be 0.
        }
        return size;
    }

    public void delete(String csvCollectionId, String fileName) {
        deleteInDb(csvCollectionId, fileName);
        getFile(csvCollectionId, fileName).delete();
    }

    public void deleteAll(String csvCollectionId) {
        deleteAllInDb(csvCollectionId, true);
    }

    public void deleteAllInDb(String csvCollectionId, boolean removeFromDb) {
        if (removeFromDb) {
            deleteAllInDb(csvCollectionId);
        }
        File[] files = getFiles(csvCollectionId);
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

}
