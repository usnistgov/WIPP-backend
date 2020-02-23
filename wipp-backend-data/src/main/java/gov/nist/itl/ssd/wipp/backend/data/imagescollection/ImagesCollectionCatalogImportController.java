package gov.nist.itl.ssd.wipp.backend.data.imagescollection;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection.ImagesCollectionImportMethod;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.files.FileHandler;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.Image;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageConversionService;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageHandler;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageRepository;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles.MetadataFileHandler;
import io.swagger.annotations.Api;

/**
 *
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */

@RestController
@Api(tags="ImagesCollection Entity")
@RequestMapping(CoreConfig.BASE_URI + "/imagesCollections/{imagesCollectionId}/catalogimport")
public class ImagesCollectionCatalogImportController {
	
	@Autowired
	CoreConfig config;

	@Autowired
	private ImagesCollectionRepository imagesCollectionRepository;

	@Autowired
	private ImageHandler imageHandler;

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private MetadataFileHandler metadataHandler;

	@Autowired
	private ImageConversionService imageConversionService;

	@RequestMapping(value = "", method = RequestMethod.POST)
	// Before importing from catalog, we make sure the user is logged in and has the right to access the collection
	@PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizeImagesCollectionId(#imagesCollectionId)")
	public void importFromCatalog(
			@PathVariable("imagesCollectionId") String imagesCollectionId) throws IOException {

		Optional<ImagesCollection> tc = imagesCollectionRepository.findById(imagesCollectionId);

		if (!tc.isPresent()) {
			throw new ResourceNotFoundException(
					"Images collection " + imagesCollectionId + " not found.");
		}

		ImagesCollection imagesCollection = tc.get();
		
		// Check if the images collection is not empty
		if(imagesCollection.getNumberOfImages() != 0  || imagesCollection.getNumberOfMetadataFiles() != 0) {
			throw new ClientException("Collection is not empty.");
		}

		// Check if the import method is CATALOG
		if(imagesCollection.getImportMethod() == null || !imagesCollection.getImportMethod().equals(ImagesCollectionImportMethod.CATALOG)){
			throw new ClientException("Import method is not CATALOG.");
		}

		// Check if sourceCatalog string is empty
		if(imagesCollection.getSourceCatalog() == null || imagesCollection.getSourceCatalog().isEmpty()){
			throw new ClientException("Source catalog cannot be empty.");
		}

		try {
			File imagesCollectionTempFolder = new File(config.getCollectionsUploadTmpFolder(), imagesCollectionId);
			
			// Import and convert images
			imageHandler.addAllInDbFromTemp(imagesCollectionId);
			List<Image> images = imageRepository.findByImagesCollection(imagesCollectionId);

			for(Image image : images) {
				imageConversionService.submitImageToExtractor(image);
			}
			
			// Import metadata files
			File metadataFolder = new File(imagesCollectionTempFolder, "metadata_files");
			if(metadataFolder.exists()) {
				importFolder(metadataHandler, metadataFolder, imagesCollectionId);
			}
						
		} catch (IOException ex) {
			throw new ClientException("Error while importing data.");
		}
	}

	private void importFolder(FileHandler fileHandler, File file, String id) throws IOException {
		fileHandler.importFolder(id, file);
	}

}
