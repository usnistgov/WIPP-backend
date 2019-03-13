package gov.nist.itl.ssd.wipp.backend.images.imagescollection;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.images.imagescollection.images.ImageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class ImagesCollectionDataHandler extends DataHandler {

    @Autowired
    CoreConfig config;

    @Autowired
    private ImagesCollectionRepository imagesCollectionRepository;

    @Autowired
    private ImageHandler imageRepository;

    public ImagesCollectionDataHandler() {
    }

    @Override
    public void importData(Job job, String outputName) throws IOException {
        ImagesCollection outputImagesCollection = new ImagesCollection(job, outputName);
        outputImagesCollection = imagesCollectionRepository.save(
                outputImagesCollection);
        try {
            imageRepository.importFolder(outputImagesCollection.getId(),
                    // new File(getJobTempFolder(job), "images"));
                    // TODO: output conventions for plugins
                    getJobTempFolder(job));
        } catch (IOException ex) {
            imagesCollectionRepository.delete(outputImagesCollection);
            throw ex;
        }
    }

    public String exportDataAsParam(String value) {
        String imagesCollectionId = value;
        File inputImagesFolder = imageRepository.getFilesFolder(imagesCollectionId);
        String imagesCollectionPath = inputImagesFolder.getAbsolutePath();
        return imagesCollectionPath;
    }

    private final File getJobTempFolder(Job job) {
        return new File(config.getJobsTempFolder(), job.getId());
    }
}
