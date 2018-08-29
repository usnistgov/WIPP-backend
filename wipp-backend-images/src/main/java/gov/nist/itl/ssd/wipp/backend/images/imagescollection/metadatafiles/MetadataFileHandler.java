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
package gov.nist.itl.ssd.wipp.backend.images.imagescollection.metadatafiles;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.images.imagescollection.ImagesCollectionRepository;
import gov.nist.itl.ssd.wipp.backend.images.imagescollection.files.FileHandler;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 */
@Component
public class MetadataFileHandler extends FileHandler {

    @Autowired
    private MetadataFileRepository metadataFileRepository;

    @Autowired
    private ImagesCollectionRepository imagesCollectionRepository;

    @Override
    protected String getSubFolder() {
        return "metadata_files";
    }

    @Override
    protected void addAllInDb(String imagesCollectionId) {
        File[] files = getFiles(imagesCollectionId);
        if (files == null) {
            return;
        }

        List<MetadataFile> metadataFiles = Arrays.stream(files).map(
                f -> new MetadataFile(
                        imagesCollectionId, f.getName(), getFileSize(f)))
                .collect(Collectors.toList());

        metadataFileRepository.saveAll(metadataFiles);
        imagesCollectionRepository.updateMetadataFilesCaches(imagesCollectionId);
    }

    @Override
    protected void deleteAllInDb(String imagesCollectionId) {
        metadataFileRepository.deleteByImagesCollection(imagesCollectionId);
        imagesCollectionRepository.updateMetadataFilesCaches(imagesCollectionId);
    }

    @Override
    protected void deleteInDb(String imagesCollectionId, String fileName) {
        metadataFileRepository.deleteByImagesCollectionAndFileName(
                imagesCollectionId, fileName);
        imagesCollectionRepository.updateMetadataFilesCaches(imagesCollectionId);
    }

}
