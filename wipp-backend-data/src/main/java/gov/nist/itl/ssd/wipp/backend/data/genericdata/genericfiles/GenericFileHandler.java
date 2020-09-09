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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.data.genericdata.GenericDataRepository;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi@nist.gov>
*/
@Component
public class GenericFileHandler {
	
    @Autowired
    private GenericFileRepository genericFileRepository;

    @Autowired
    private GenericDataRepository genericDataRepository;

    @Autowired
    private CoreConfig config;

    protected void addAllInDb(String genericDataId) {
        File[] files = getFiles(genericDataId);
        if (files == null) {
            return;
        }

        List<GenericFile> genericFiles = Arrays.stream(files).map(
                f -> new GenericFile(
                		genericDataId, f.getName(), f.getName(), getFileSize(f), true))
                .collect(Collectors.toList());

        genericFileRepository.saveAll(genericFiles);
        genericDataRepository.updateGenericFileCaches(genericDataId);
    }

    protected void deleteAllInDb(String genericDataId) {
    	genericFileRepository.deleteByGenericData(genericDataId);
        genericDataRepository.updateGenericFileCaches(genericDataId);
    }

    protected void deleteInDb(String genericDataId, String fileName) {
    	genericFileRepository.deleteByGenericDataAndFileName(genericDataId, fileName);
        genericDataRepository.updateGenericFileCaches(genericDataId);
    }

    public void importFolder(String genericDataId, File folder)
            throws IOException {
        File genericFilesFolder = getFilesFolder(genericDataId);
        genericFilesFolder.getParentFile().mkdirs();
        Files.move(folder.toPath(), genericFilesFolder.toPath());
        addAllInDb(genericDataId);
    }

    public File getFile(String genericDataId, String fileName) {
        return new File(getFilesFolder(genericDataId), fileName);
    }

    protected File[] getFiles(String genericDataId) {
        return getFilesFolder(genericDataId).listFiles(File::isFile);
    }

    public File getFilesFolder(String genericDataId) {
        return new File(config.getGenericDatasFolder(), genericDataId);
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

    public void delete(String genericDataId, String fileName) {
        deleteInDb(genericDataId, fileName);
        getFile(genericDataId, fileName).delete();
    }

    public void deleteAll(String genericDataId) {
        deleteAllInDb(genericDataId, true);
    }

    public void deleteAllInDb(String genericDataId, boolean removeFromDb) {
        if (removeFromDb) {
            deleteAllInDb(genericDataId);
        }
        File[] files = getFiles(genericDataId);
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

}
