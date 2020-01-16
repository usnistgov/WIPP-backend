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
package gov.nist.itl.ssd.wipp.backend.data.imagescollection.images;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.data.utils.flowjs.FlowFile;
//import io.swagger.annotations.Api;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollectionRepository;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.files.FileUploadController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@RestController
//@Api(tags="ImagesCollection Entity")
@RequestMapping(CoreConfig.BASE_URI + "/imagesCollections/{imagesCollectionId}/images")
public class ImageUploadController extends FileUploadController {

	private static final Logger LOG = Logger.getLogger(ImageUploadController.class.getName());

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private ImagesCollectionRepository imagesCollectionRepository;

	@Autowired
	private ImageConversionService imageConversionService;

	@Override
	protected String getUploadSubFolder() {
		return "images";
	}

	@Override
	protected void onUploadFinished(FlowFile flowFile, Path tempPath)
			throws IOException {
		String collectionId = getCollectionId(flowFile);
		String fileName = flowFile.getFlowFilename();

		try {
			ImagesCollection imgCol = imagesCollectionRepository.findById(collectionId).get();
			String imgColPattern = imgCol.getPattern();
			if(imgColPattern != null && !imgColPattern.isEmpty()){
				fileName = fileNameFilter(imgColPattern, fileName);
			}
		} catch (NoSuchElementException e){
			// TODO: better handling of this case
			LOG.log(Level.WARNING, "Error finding collection " + collectionId
					+ " when uploading file " + fileName,
					e);
		}

		if(fileName != null){
			fileName = fileName.replaceAll("[\\p{Punct}&&[^.-]]", "_");
			fileName = fileName.replace(" ", "");
			uploadImg(flowFile, tempPath, fileName);
		}
	}

	private void uploadImg(FlowFile flowFile, Path tempPath, String fileName) throws IOException{
		File uploadDir = getUploadDir(flowFile);
		uploadDir.mkdirs();
		String collectionId = getCollectionId(flowFile);
		Image image = new Image(collectionId, fileName, flowFile.getFlowFilename(),
				getPathSize(tempPath), true);
		imageRepository.save(image);
		imagesCollectionRepository.updateImagesCaches(collectionId);
		imageConversionService.submitImageToExtractor(image);
	}

	private static String fileNameFilter(String patternStr, String fileName){
		Pattern p = Pattern.compile(patternStr);
		Matcher m = p.matcher(fileName);
		boolean matches = m.matches();

		if(matches){
			return fileName;
		}
		return null;
	}
	
}
