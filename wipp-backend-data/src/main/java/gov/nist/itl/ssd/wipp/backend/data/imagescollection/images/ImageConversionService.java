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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection.ImagesCollectionFormat;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollectionRepository;
import gov.nist.itl.ssd.wipp.backend.data.utils.tiledtiffs.TiledOmeTiffConverter;
import gov.nist.itl.ssd.wipp.backend.data.utils.zarr.OmeZarrConverter;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.files.FileUploadBase;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;


/**
*
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
* @author Mylene Simon <mylene.simon at nist.gov>
*/
@Service
public class ImageConversionService extends FileUploadBase{
	
	private static final Logger LOG = Logger.getLogger(ImageConversionService.class.getName());

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
	
	public void submitImageToExtractor(Image image) {
		String collectionId = image.getImagesCollection();
		ImagesCollection imgCollection = imagesCollectionRepository.findById(collectionId).orElse(null);
		ImagesCollectionFormat targetFormat = imgCollection.getFormat() != null ? imgCollection.getFormat() : ImagesCollectionFormat.OMETIFF;
		File tempUploadDir = getTempUploadDir(collectionId);
		File uploadDir = getUploadDir(collectionId);
		uploadDir.mkdirs();

		String imgName = image.getFileName();
		Path tempPath = new File(tempUploadDir, image.getOriginalFileName()).toPath();
		String outputFileName;
		boolean isOmeFormat = imgName.endsWith(".ome.tif") 
				|| imgName.endsWith(".ome.tiff")
				|| imgName.endsWith(".ome.tf2")
				|| imgName.endsWith(".ome.tf8")
				|| imgName.endsWith(".ome.btf")
				|| imgName.endsWith(".ome.xml")
				|| imgName.endsWith(".ome.zarr");
		
		String outputFileNameOmeExtension = targetFormat.equals(ImagesCollectionFormat.OMEZARR) ? ".zarr"
				: ".tif";
		
		if(!targetFormat.equals(ImagesCollectionFormat.RAW)) {
			// handle ome images file names
			if(!isOmeFormat){
				outputFileName = FilenameUtils.getBaseName(imgName) + ".ome" + outputFileNameOmeExtension;
			} else {
				outputFileName = FilenameUtils.getBaseName(imgName) + outputFileNameOmeExtension;;
			}
		} else {
			outputFileName = imgName;
		}
			
		Path outputPath = new File(uploadDir, outputFileName).toPath();

		omeConverterExecutor.submit(() -> doSubmit(
				collectionId, image, targetFormat, outputFileName, tempPath, outputPath));
	}

	public void doSubmit(String collectionId, Image image, ImagesCollectionFormat format, String outputFileName,
			Path tempPath, Path outputPath) {
		try {
			LOG.log(Level.INFO,
					"Starting extracting image {0} of collection {1}",
					new Object[]{image.getFileName(), collectionId});
			
			switch(format) {
				case OMETIFF:
					convertToTiledOmeTiff(tempPath, outputPath);
					Files.delete(tempPath);
					break;
				case OMEZARR:
					convertToOmeZarr(tempPath, outputPath);
					Files.delete(tempPath);
					break;	
				case RAW:
					moveWithoutConverting(tempPath, outputPath);
					break;	
			}
			
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
	
	public static void convertToTiledOmeTiff(Path inputFile, Path outputFile) throws DependencyException, FormatException, IOException, ServiceException {
		TiledOmeTiffConverter tiledOmeTiffConverter = new TiledOmeTiffConverter(
				inputFile.toString(),
				outputFile.toString(), 
				CoreConfig.TILE_SIZE, 
				CoreConfig.TILE_SIZE);
		try {
	    	tiledOmeTiffConverter.init();
	    	tiledOmeTiffConverter.readWriteTiles();
	    }
	    catch(Exception e) {
	      throw new IOException("Cannot convert image to OME TIFF.", e);
	    }
	    finally {
	    	tiledOmeTiffConverter.cleanup();
	    }
	}
	
	// TODO: refactor
	public static void convertToOmeZarr(Path inputFile, Path outputFile) throws DependencyException, FormatException, IOException, ServiceException {
		OmeZarrConverter omeZarrConverter = new OmeZarrConverter(
				inputFile.toString(),
				outputFile.toString(), 
				CoreConfig.TILE_SIZE, 
				CoreConfig.TILE_SIZE);
		try {
			omeZarrConverter.init();
			omeZarrConverter.readWriteTiles();
	    }
	    catch(Exception e) {
	      throw new IOException("Cannot convert image to OME ZARR.", e);
	    }
	    finally {
	    	omeZarrConverter.cleanup();
	    }
	}
	
	public static void moveWithoutConverting(Path inputFile, Path outputFile) throws DependencyException, FormatException, IOException, ServiceException {
		try {
			Files.move(inputFile, outputFile);
	    }
	    catch(Exception e) {
	      throw new IOException("Cannot move image to final destination.", e);
	    }
	}
}
