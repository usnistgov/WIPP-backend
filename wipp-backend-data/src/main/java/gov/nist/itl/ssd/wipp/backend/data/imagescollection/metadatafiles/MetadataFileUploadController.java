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
package gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.data.utils.flowjs.FlowFile;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollectionRepository;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.files.FileUploadController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * Adapted by Mohamed Ouladi <mohamed.ouladi@nist.gov>
 */
@RestController
@Tag(name="ImagesCollection Entity")
@RequestMapping(CoreConfig.BASE_URI + "/imagesCollections/{imagesCollectionId}/metadataFiles")
public class MetadataFileUploadController extends FileUploadController {

    @Autowired
    private MetadataFileRepository metadataFileRepository;

    @Autowired
    private ImagesCollectionRepository imagesCollectionRepository;

    @Override
    protected String getUploadSubFolder() {
        return "metadata_files";
    }

    @Override
    protected void onUploadFinished(FlowFile flowFile, Path tempPath)
            throws IOException {
        File uploadDir = getUploadDir(flowFile);
        uploadDir.mkdirs();
        String fileName = flowFile.getFlowFilename();

        if(!fileName.equals(".DS_Store") && !fileName.equalsIgnoreCase("thumbs.db")){
	        Path path = Files.move(tempPath,
	                new File(uploadDir, fileName).toPath(),
	                StandardCopyOption.REPLACE_EXISTING);
	        String collectionId = getCollectionId(flowFile);
	        metadataFileRepository.save(new MetadataFile(
	                collectionId,
	                path.getFileName().toString(),
	                getPathSize(path)));
	        imagesCollectionRepository.updateMetadataFilesCaches(collectionId);
        }
    }
}
