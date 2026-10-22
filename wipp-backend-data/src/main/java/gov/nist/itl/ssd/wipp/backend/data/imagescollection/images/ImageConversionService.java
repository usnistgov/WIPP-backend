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
import java.util.Optional;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.glencoesoftware.bioformats2raw.Converter;
import com.glencoesoftware.pyramid.CompressionType;
import com.glencoesoftware.pyramid.PyramidFromDirectoryWriter;
import gov.nist.itl.ssd.wipp.backend.core.utils.SecurityUtils;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection;
import jakarta.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollectionRepository;
import gov.nist.itl.ssd.wipp.backend.data.utils.tiledtiffs.TiledOmeTiffConverter;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.files.FileUploadBase;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import org.springframework.util.FileSystemUtils;
import picocli.CommandLine;


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
		omeConverterExecutor = Executors.newFixedThreadPool(1);

		// Load security context for system operations
		SecurityUtils.runAsSystem();

		// Resume any interrupted conversion
		imageRepository.findByImporting(true)
                .forEach(image -> {
                    Optional<ImagesCollection> coll = imagesCollectionRepository.findById(image.getImagesCollection());
					if (coll.isPresent() && ImagesCollection.ImagesCollectionImportMethod.BACKEND_IMPORT.equals(
							coll.get().getImportMethod())) {
						File sourceDir = new File(appConfig.getLocalImportFolder(), coll.get().getSourceBackendImport());
						this.submitImageToExtractor(image, sourceDir, false);
					} else {
						this.submitImageToExtractor(image);
					}
                });
		// Clear security context after system operations
		SecurityContextHolder.clearContext();
	}

	@Override
	protected String getUploadSubFolder() {
		return "images";
	}
	
	public void submitImageToExtractor(Image image) {
		File tempUploadDir = getTempUploadDir(image.getImagesCollection());
		this.submitImageToExtractor(image, tempUploadDir, true);
	}

	public void submitImageToExtractor(Image image, File sourceDir, boolean deleteSourceImage) {
		String collectionId = image.getImagesCollection();
		ImagesCollection imgCollection = imagesCollectionRepository.findById(collectionId).orElse(null);
		// if images collection not found, image should be deleted to avoid inconsistent state
		if (imgCollection == null) {
		    LOG.warning("Images Collection not found for image " + image.getFileName()
			    + " while attempting to convert, deleting.");
		    imageRepository.delete(image);
		    return;
		}
		File tempUploadDir = sourceDir;
		File uploadDir = getUploadDir(image.getImagesCollection());
		uploadDir.mkdirs();

		String imgName = image.getFileName();
		Path tempPath;
		if(image.getOriginalFilePath() != null && !image.getOriginalFilePath().isBlank()) {
			tempPath = new File(tempUploadDir, image.getOriginalFilePath()).toPath();
		} else {
			tempPath = new File(tempUploadDir, image.getOriginalFileName()).toPath();
		}
		String outputFileName;
		boolean isOmeTiff = imgName.endsWith(".ome.tif");

		// handle ome tiff images file names
		if(!isOmeTiff){
			outputFileName = FilenameUtils.getBaseName(imgName) + ".ome.tif";
		} else {
			outputFileName = imgName;
		}

		Path outputPath = new File(uploadDir, outputFileName).toPath();

		omeConverterExecutor.submit(() -> doSubmit(
				collectionId, image, outputFileName, tempPath, outputPath, deleteSourceImage));
	}

	public void doSubmit(String collectionId, Image image, String outputFileName,
			Path tempPath, Path outputPath, boolean deleteSourceImage) {
		try {
			LOG.log(Level.INFO,
					"Starting extracting image {0} of collection {1}",
					new Object[]{image.getFileName(), collectionId});
			convertToTiledOmeTiff(tempPath, outputPath);
			if (deleteSourceImage) {
				Files.delete(tempPath);
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
	
	public void convertToTiledOmeTiff(Path inputFile, Path outputFile) throws DependencyException, FormatException,
			IOException, ServiceException {
		String omeTiffOutputName = outputFile.toString();
		String omeZarrOutputName = omeTiffOutputName.substring(0, omeTiffOutputName.lastIndexOf('.')) + ".zarr";

		try {
	    	// First convert to OME NGFF/ZARR pyramid (downsampling step)
	    	String[] converterArgs = new String[]{
	    			inputFile.toString(),
	    			omeZarrOutputName,
	    			"--tile-height", String.valueOf(CoreConfig.TILE_SIZE),
	    			"--tile-width", String.valueOf(CoreConfig.TILE_SIZE),
					"--max-workers", String.valueOf(appConfig.getOmeConverterThreads()),
					"--log-level", "ERROR"
	    	};
	    	CommandLine.call(new Converter(), converterArgs);

	    	// Then convert to OME TIFF pyramid
	    	String[] converterPyrArgs = new String[]{
	    			omeZarrOutputName,
	    			outputFile.toString(),
	    			"--rgb",
	    			"--compression", "LZW",
					"--max_workers", String.valueOf(appConfig.getOmeConverterThreads()),
					"--log-level", "ERROR"
	    	};
	    	CommandLine.call(new PyramidFromDirectoryWriter(), converterPyrArgs);

		} catch (Exception e) {
	    	throw new IOException("Cannot convert image to OME TIFF.", e);
	    } finally {
	    	// cleanup temporary OME ZARR directory
	    	Path omeZarrDir = Paths.get(omeZarrOutputName);
	    	if(Files.exists(omeZarrDir)) {
	    		FileSystemUtils.deleteRecursively(omeZarrDir);
	    	}
		}
	}
}
