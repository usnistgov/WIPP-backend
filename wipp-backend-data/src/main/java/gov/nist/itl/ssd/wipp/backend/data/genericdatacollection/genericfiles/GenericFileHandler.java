package gov.nist.itl.ssd.wipp.backend.data.genericdatacollection.genericfiles;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.data.genericdatacollection.GenericDataCollectionRepository;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
*/
@Component
public class GenericFileHandler {

    @Autowired
    private GenericFileRepository genericFileRepository;

    @Autowired
    private GenericDataCollectionRepository genericDataCollectionRepository;

    @Autowired
    private CoreConfig config;

    protected void addAllInDb(String genericDataCollectionId) {
        File[] files = getFiles(genericDataCollectionId);
        if (files == null) {
            return;
        }

        List<GenericFile> genericFiles = Arrays.stream(files).map(
                f -> new GenericFile(
                		genericDataCollectionId, f.getName(), f.getName(), getFileSize(f), true))
                .collect(Collectors.toList());

        genericFileRepository.saveAll(genericFiles);
        genericDataCollectionRepository.updateGenericFilesCaches(genericDataCollectionId);
    }

    protected void deleteAllInDb(String genericDataCollectionId) {
    	genericFileRepository.deleteByGenericDataCollection(genericDataCollectionId);
    	genericDataCollectionRepository.updateGenericFilesCaches(genericDataCollectionId);
    }

    protected void deleteInDb(String genericDataCollectionId, String fileName) {
    	genericFileRepository.deleteByGenericDataCollectionAndFileName(genericDataCollectionId, fileName);
    	genericDataCollectionRepository.updateGenericFilesCaches(genericDataCollectionId);
    }

    public void importFolder(String genericDataCollectionId, File folder)
            throws IOException {
        File genericFilesFolder = getFilesFolder(genericDataCollectionId);
        genericFilesFolder.getParentFile().mkdirs();
        Files.move(folder.toPath(), genericFilesFolder.toPath());
        addAllInDb(genericDataCollectionId);
    }

    public File getFile(String genericDataCollectionId, String fileName) {
        return new File(getFilesFolder(genericDataCollectionId), fileName);
    }

    protected File[] getFiles(String genericDataCollectionId) {
        return getFilesFolder(genericDataCollectionId).listFiles(File::isFile);
    }

    public File getFilesFolder(String genericDataCollectionId) {
        return new File(config.getGenericDataCollectionsFolder(), genericDataCollectionId);
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

    public void delete(String genericDataCollectionId, String fileName) {
        deleteInDb(genericDataCollectionId, fileName);
        getFile(genericDataCollectionId, fileName).delete();
    }

    public void deleteAll(String genericDataCollectionId) {
        deleteAllInDb(genericDataCollectionId, true);
    }

    public void deleteAllInDb(String genericDataCollectionId, boolean removeFromDb) {
        if (removeFromDb) {
            deleteAllInDb(genericDataCollectionId);
        }
        File[] files = getFiles(genericDataCollectionId);
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }
}
