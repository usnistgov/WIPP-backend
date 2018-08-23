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
package gov.nist.itl.ssd.wipp.backend.core.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

/**
 *
 * @author Antoine Vandecreme
 */
public abstract class FilenameConverter {

    public abstract boolean canConvert(String fileName);

    public abstract String convert(String fileName);

    /**
     * Returns a FilenameConverter with sourcePattern and destPattern
     * interchanged.
     *
     * @return the opposite FilenameConverter
     */
    public abstract FilenameConverter createOpposite();

    /**
     * Convert the given file by replacing it parent by newParent and its
     * fileName by the conversion method.
     *
     * @param file
     * @param newParent
     * @return
     */
    public File convert(File file, File newParent) {
        return new File(newParent, convert(file.getName()));
    }

    /**
     * Equivalent to File.listFiles method but converting file names.
     *
     * @param folderToList the folder on which File.listFiles is called
     * @param newFolder the folder to replace folderToList in the results
     * @param filter the filter
     * @return the files list
     */
    public File[] listFiles(File folderToList, File newFolder,
            FileFilter filter) {
        File[] files = folderToList.listFiles(filter);
        if (files == null) {
            return null;
        }
        return Arrays.stream(files).map(f -> convert(f, newFolder))
                .toArray(File[]::new);
    }

}
