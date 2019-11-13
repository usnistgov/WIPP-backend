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
package gov.nist.itl.ssd.wipp.backend.data.imagescollection;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.files.FileHandler;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageHandler;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles.MetadataFileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import gov.nist.itl.ssd.wipp.backend.core.model.data.BaseDataHandler;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Samia Benjida <samia.benjida at nist.gov>
 */
@Component("collectionDataHandler")
public class ImagesCollectionDataHandler extends BaseDataHandler implements DataHandler {

    @Autowired
    CoreConfig config;

    @Autowired
    private ImagesCollectionRepository imagesCollectionRepository;

    @Autowired
    private ImageHandler imageRepository;

    @Autowired
    private MetadataFileHandler metadataRepository;

    public ImagesCollectionDataHandler() {
    }

    @Override
    public void importData(Job job, String outputName) throws IOException {
        ImagesCollection outputImagesCollection = new ImagesCollection(job, outputName);
        outputImagesCollection = imagesCollectionRepository.save(
                outputImagesCollection);

        String imagesCollectionId = outputImagesCollection.getId();
        try {
            File jobOutputTempFolder = getJobOutputTempFolder(job.getId(), outputName);
            File metadataFolder = new File(jobOutputTempFolder, "metadata_files");
            File imagesFolder = new File(jobOutputTempFolder, "images");

            if (metadataFolder.exists() || imagesFolder.exists()) {
                if (imagesFolder.exists()) {
                    importFolder(imageRepository, imagesFolder, imagesCollectionId);
                }
                if(metadataFolder.exists()) {
                    importFolder(metadataRepository, metadataFolder, imagesCollectionId);
                }
            }
            else {
                importFolder(imageRepository, jobOutputTempFolder, imagesCollectionId);
            }
            setOutputId(job, outputName, imagesCollectionId);

        } catch (IOException ex) {
            imagesCollectionRepository.delete(outputImagesCollection);
            throw ex;
        }
    }

    public String exportDataAsParam(String value) {
        String imagesCollectionId = value;
        String imagesCollectionPath;

        // check if the input of the job is the output of another job and if so return the associated path
        String regex = "\\{\\{ (.*)\\.(.*) \\}\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(imagesCollectionId);
        if (m.find()) {
            String jobId = m.group(1);
            String outputName = m.group(2);
            imagesCollectionPath = getJobOutputTempFolder(jobId, outputName).getAbsolutePath();
        }
        // else return the path of the regular images collection
        else {
            File inputImagesFolder = imageRepository.getFilesFolder(imagesCollectionId);
            imagesCollectionPath = inputImagesFolder.getAbsolutePath();

        }
        imagesCollectionPath = imagesCollectionPath.replace("\\","/").replaceFirst(config.getStorageRootFolder(),config.getContainerInputsMountPath());
        return imagesCollectionPath;
    }

    private void importFolder(FileHandler fileHandler, File file, String id) throws IOException {
        fileHandler.importFolder(id, file);
    }
}
