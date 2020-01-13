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
import gov.nist.itl.ssd.wipp.backend.core.utils.PatternFilenameConverter;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 */
public abstract class FileHandler {

    @Autowired
    private CoreConfig config;

    protected abstract String getSubFolder();

    protected abstract void addAllInDb(String imagesCollectionId);

    protected abstract void deleteAllInDb(String imagesCollectionId);

    protected abstract void deleteInDb(
            String imagesCollectionId, String fileName);

    public void importFolder(String imagesCollectionId, File folder)
            throws IOException {
        File imagesFolder = getFilesFolder(imagesCollectionId);
        imagesFolder.getParentFile().mkdirs();
        Files.move(folder.toPath(), imagesFolder.toPath());
        addAllInDb(imagesCollectionId);
    }

    /**
     * Copy all the files from the images collection "fromId" to the images
     * collection "toId".
     *
     * @param fromId
     * @param toId
     * @throws IOException
     */
    public void copy(String fromId, String toId) throws IOException {
        File fromFolder = getFilesFolder(fromId);
        if (!fromFolder.exists()) {
            return;
        }
        File toFolder = getFilesFolder(toId);
        FileUtils.copyDirectory(fromFolder, toFolder);
        addAllInDb(toId);
    }

    /**
     * Copy all the files matching the source pattern from the images collection
     * "fromId" to the images collection "toId" with the destination pattern.
     *
     * @param fromId
     * @param toId
     * @param sourcePattern
     * @param destPattern
     * @throws IOException
     */
    public void copy(String fromId, String toId,
            String sourcePattern, String destPattern) throws IOException {
        File fromFolder = getFilesFolder(fromId);
        if (!fromFolder.exists()) {
            return;
        }
        File toFolder = getFilesFolder(toId);
        toFolder.mkdirs();
        PatternFilenameConverter converter
                = new PatternFilenameConverter(sourcePattern, destPattern);
        String[] files = fromFolder.list(
                (File dir, String name) -> converter.canConvert(name));
        for (String file : files) {
            FileUtils.copyFile(
                    new File(fromFolder, file),
                    new File(toFolder, converter.convert(file)));
        }
        addAllInDb(toId);
    }

    public InputStream getInputStream(String imagesCollectionId,
            String fileName) throws FileNotFoundException {
        return new FileInputStream(getFile(imagesCollectionId, fileName));
    }

    public void deleteAll(String imagesCollectionId) {
        deleteAll(imagesCollectionId, true);
    }

    public void deleteAll(String imagesCollectionId, boolean removeFromDb) {
        if (removeFromDb) {
            deleteAllInDb(imagesCollectionId);
        }
        File[] files = getFiles(imagesCollectionId);
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public void delete(String imagesCollectionId, String fileName) {
        deleteInDb(imagesCollectionId, fileName);
        getFile(imagesCollectionId, fileName).delete();
    }

    public File getFilesFolder(ImagesCollection imagesCollection) {
        return getFilesFolder(imagesCollection.getId());
    }

    public File getFilesFolder(String imagesCollectionId) {
        return new File(
                new File(config.getImagesCollectionsFolder(), imagesCollectionId),
                getSubFolder());
    }
    
    public File getTempFilesFolder(String imagesCollectionId) {
        return new File(
                new File(config.getCollectionsUploadTmpFolder(), imagesCollectionId),
                getSubFolder());
    }

    public File getFile(String imagesCollectionId, String fileName) {
        return new File(getFilesFolder(imagesCollectionId), fileName);
    }

    protected File[] getFiles(String imagesCollectionId) {
        return getFilesFolder(imagesCollectionId).listFiles(File::isFile);
    }
    
    protected File[] getTempFiles(String imagesCollectionId) {
        return getTempFilesFolder(imagesCollectionId).listFiles(File::isFile);
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
}
