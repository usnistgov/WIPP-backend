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
package gov.nist.itl.ssd.wipp.backend.images.imagescollection.images;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.images.flowjs.FlowFile;
import gov.nist.itl.ssd.wipp.backend.images.imagescollection.ImagesCollection;
import gov.nist.itl.ssd.wipp.backend.images.imagescollection.ImagesCollectionRepository;
import gov.nist.itl.ssd.wipp.backend.images.imagescollection.ImagesCollection.UploadOption;
import gov.nist.itl.ssd.wipp.backend.images.imagescollection.files.FileUploadController;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.codec.CompressionType;
import loci.formats.gui.BufferedImageReader;
import loci.formats.gui.BufferedImageWriter;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLService;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * Adapted by Mohamed Ouladi <mohamed.ouladi@nist.gov>
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
        Path fileRelativePath =  Paths.get(flowFile.getFlowRelativePath());
    	UploadOption uploadOption = UploadOption.REGULAR;
    	
    	try {
    		ImagesCollection imgCol = imagesCollectionRepository.findById(collectionId).get();
    		uploadOption = imgCol.getUploadOption();
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
	    	switch (uploadOption) {
	    		case REGULAR:
	    			uploadImg(flowFile, tempPath, fileName);
	    			break;
	    		case IGNORE_SUBS:
	    			if (fileRelativePath.getNameCount() <= 2){
	    				uploadImg(flowFile, tempPath, fileName);
	    			}
	    			break;
	    		case INCLUDE_PATH_IMG_NAME:
	    			fileName = flowFile.getFlowRelativePath().replace("/", "_").replaceAll("[\\p{Punct}&&[^.-]]", "_").replace(" ", "");
	    			uploadImg(flowFile, tempPath, fileName);
	    			break;
	    		default:
					uploadImg(flowFile, tempPath, fileName);
					break;
	    	}
        }
    }
    
    private void uploadImg(FlowFile flowFile, Path tempPath, String fileName) throws IOException{
    	 File uploadDir = getUploadDir(flowFile);
         uploadDir.mkdirs();
         
         boolean isOmeTiff = fileName.endsWith(".ome.tif");
         String collectionId = getCollectionId(flowFile);
        
         if (!isOmeTiff) {
             Image image = new Image(collectionId, fileName, flowFile.getFlowFilename(),
                     getPathSize(tempPath), true);
             imageRepository.save(image);
             imagesCollectionRepository.updateImagesCaches(collectionId);
             submitImageToExtractor(image);
         } else {
             Path outputPath = new File(uploadDir, fileName).toPath();
             Files.move(tempPath, outputPath,
                     StandardCopyOption.REPLACE_EXISTING);
             imageRepository.save(new Image(
                     collectionId, fileName, flowFile.getFlowFilename(), getPathSize(outputPath), false));
             imagesCollectionRepository.updateImagesCaches(collectionId);
         }
    }
    
    	
    private void submitImageToExtractor(Image image) {
        String collectionId = image.getImagesCollection();
        File tempUploadDir = getTempUploadDir(collectionId);
        File uploadDir = getUploadDir(collectionId);
        
        String imgName = image.getFileName();
        Path tempPath = new File(tempUploadDir, image.getOriginalFileName()).toPath();
        String outputFileName = FilenameUtils.getBaseName(imgName)
                + ".ome.tif";
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
            convertToOmeTiff(tempPath, outputPath);
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

    private static void convertToOmeTiff(Path inputImage, Path outputOmeTiff)
            throws IOException {
        try {
            ServiceFactory factory = new ServiceFactory();
            OMEXMLService service = factory.getInstance(OMEXMLService.class);

            OMEXMLMetadata metadataStore;
            BufferedImage bufferedImage;
            String inputPath = inputImage.toString();
            try (ImageReader imageReader = new ImageReader()) {
                IFormatReader reader = imageReader.getReader(inputPath);
                reader.setOriginalMetadataPopulated(true);
                metadataStore = (OMEXMLMetadata) service.createOMEXMLMetadata();
                reader.setMetadataStore(metadataStore);
                reader.setId(inputPath);

                try (BufferedImageReader bir = new BufferedImageReader(
                        imageReader)) {
                    bufferedImage = bir.openImage(0);
                }
            }

            // Important to delete because OME uses RandomAccessFile
            outputOmeTiff.toFile().delete();
            String outputPath = outputOmeTiff.toString();
            try (BufferedImageWriter imageWriter = new BufferedImageWriter()) {
                imageWriter.setMetadataRetrieve(metadataStore);
                imageWriter.setId(outputPath);
                imageWriter.setCompression(CompressionType.LZW.getCompression());
                imageWriter.saveImage(0, bufferedImage);
            }
        } catch (FormatException | DependencyException | ServiceException ex) {
            throw new IOException("Cannot convert image to OME TIFF.", ex);
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
    
}
