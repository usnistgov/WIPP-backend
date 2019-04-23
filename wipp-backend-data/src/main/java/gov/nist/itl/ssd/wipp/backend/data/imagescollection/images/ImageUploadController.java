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
import gov.nist.itl.ssd.wipp.backend.data.utils.tiledtiffs.TiledOmeTiffConverter;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollectionRepository;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.files.FileUploadController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;


import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@RestController
@RequestMapping(CoreConfig.BASE_URI + "/imagesCollections/{imagesCollectionId}/images")
public class ImageUploadController extends FileUploadController {

	private static final Logger LOG = Logger.getLogger(ImageUploadController.class.getName());

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private ImagesCollectionRepository imagesCollectionRepository;

	@Autowired
	private CoreConfig appConfig;

	private ExecutorService omeConverterExecutor;

	@PostConstruct
	public void instantiateOmeConverter() {
		omeConverterExecutor = Executors.newFixedThreadPool(
				appConfig.getOmeConverterThreads());

		// Resume any interrupted conversion
		imageRepository.findByImporting(true)
		.forEach(this::submitImageToExtractor);
	}

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
		submitImageToExtractor(image);
	}

	private void submitImageToExtractor(Image image) {
		String collectionId = image.getImagesCollection();
		File tempUploadDir = getTempUploadDir(collectionId);
		File uploadDir = getUploadDir(collectionId);

		String imgName = image.getFileName();
		Path tempPath = new File(tempUploadDir, image.getOriginalFileName()).toPath();
		String outputFileName;
		boolean isOmeTiff = imgName.endsWith(".ome.tif");
		
		// handle ome tiff images file names
		if(!isOmeTiff){
			outputFileName = FilenameUtils.getBaseName(imgName) + ".ome.tif";
		} else {
			outputFileName = FilenameUtils.getBaseName(imgName) + ".tif";
		}
		
		Path outputPath = new File(uploadDir, outputFileName).toPath();

		omeConverterExecutor.submit(() -> doSubmit(
				collectionId, image, outputFileName, tempPath, outputPath));
	}

	private void doSubmit(String collectionId, Image image, String outputFileName,
			Path tempPath, Path outputPath) {
		try {
			LOG.log(Level.INFO,
					"Starting extracting image {0} of collection {1}",
					new Object[]{image.getFileName(), collectionId});
			//convertToOmeTiff(tempPath, outputPath);
			convertToTiledOmeTiff(tempPath, outputPath);
			Files.delete(tempPath);
			image.setFileName(outputFileName);
			image.setFileSize(getPathSize(outputPath));
			image.setImporting(false);
			imageRepository.save(image);
			imagesCollectionRepository.updateImagesCaches(collectionId);
			LOG.log(Level.INFO,
					"Done extracting image {0} of collection {1}",
					new Object[]{image.getFileName(), collectionId});
		} catch (Exception ex) {
			LOG.log(Level.WARNING, "Error extracting image "
					+ image.getFileName() + " of collection " + collectionId,
					ex);
			// Update image
			image.setImporting(false);
			image.setImportError("Can not extract image.");
			imageRepository.save(image);
			imagesCollectionRepository.updateImagesCaches(collectionId);
		}
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
	
	private static void convertToTiledOmeTiff(Path inputFile, Path outputFile) throws DependencyException, FormatException, IOException, ServiceException {
		TiledOmeTiffConverter tiledOmeTiffConverter = new TiledOmeTiffConverter(inputFile.toString(), outputFile.toString(), CoreConfig.TILE_SIZE, CoreConfig.TILE_SIZE);
		
		tiledOmeTiffConverter.init();

	    try {
	    	tiledOmeTiffConverter.readWriteTiles()  ;
	    }
	    catch(Exception e) {
	      System.err.println("Failed to read and write tiles.");
	      e.printStackTrace();
	      throw e;
	    }
	    finally {
	    	tiledOmeTiffConverter.cleanup();
	    }
	}
	
}
