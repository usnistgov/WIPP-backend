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
package gov.nist.itl.ssd.wipp.backend.data.genericdatacollection;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataDownloadToken;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataDownloadTokenRepository;
import gov.nist.itl.ssd.wipp.backend.core.rest.DownloadUrl;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.utils.SecurityUtils;
import io.swagger.annotations.Api;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
*/
@Controller
@Api(tags="GenericDataCollection Entity")
@RequestMapping(CoreConfig.BASE_URI + "/genericDataCollections/{genericDataCollectionId}/download")
public class GenericDataCollectionDownloadController {

	@Autowired
	CoreConfig config;

	@Autowired
	GenericDataCollectionRepository genericDataCollectionRepository;
	
	@Autowired
	DataDownloadTokenRepository dataDownloadTokenRepository;

	@RequestMapping(
            value = "request",
            method = RequestMethod.GET,
            produces = "application/json")
	@PreAuthorize("hasRole('admin') or @genericDataCollectionSecurity.checkAuthorize(#genericDataCollectionId, false)")
	public DownloadUrl requestDownload(
            @PathVariable("genericDataCollectionId") String genericDataCollectionId) {
    	
    	// Check existence of data
    	Optional<GenericDataCollection> gd = genericDataCollectionRepository.findById(
    			genericDataCollectionId);
        if (!gd.isPresent()) {
            throw new ResourceNotFoundException(
                    "Generic data collection " + genericDataCollectionId + " not found.");
        }
        
        // Generate download token
        DataDownloadToken downloadToken = new DataDownloadToken(genericDataCollectionId);
        dataDownloadTokenRepository.save(downloadToken);
        
        // Generate and send unique download URL
        String tokenParam = "?token=" + downloadToken.getToken();
        String downloadLink = linkTo(GenericDataCollectionDownloadController.class,
        		genericDataCollectionId).toString() + tokenParam;
        return new DownloadUrl(downloadLink);
    }
	
	@RequestMapping(
			value = "",
			method = RequestMethod.GET,
			produces = "application/zip")
	public void get(
			@PathVariable("genericDataCollectionId") String genericDataCollectionId,
			@RequestParam("token")String token,
			HttpServletResponse response) throws IOException {
		
    	// Load security context for system operations
    	SecurityUtils.runAsSystem();
		
    	// Check validity of download token
    	Optional<DataDownloadToken> downloadToken = dataDownloadTokenRepository.findByToken(token);
    	if (!downloadToken.isPresent() || !downloadToken.get().getDataId().equals(genericDataCollectionId)) {
    		throw new ForbiddenException("Invalid download token.");
    	}
    	
    	// Check existence of data
		GenericDataCollection genericDataCollection = null;
		Optional<GenericDataCollection> optGenericDataCollection = genericDataCollectionRepository.findById(genericDataCollectionId);
		
		if (!optGenericDataCollection.isPresent()) {
			throw new ResourceNotFoundException(
					"Generic Data Collection " + genericDataCollectionId + " not found.");
		} else { // generic data collection is present
			genericDataCollection = optGenericDataCollection.get();
        }

		// get generic data collection folder
		File genericDataStorageFolder = new File(config.getGenericDataCollectionsFolder(), genericDataCollection.getId());
		if (! genericDataStorageFolder.exists()) {
			throw new ResourceNotFoundException(
					"Generic data collection " + genericDataCollectionId + " " + genericDataCollection.getName() + " not found.");
		}

		response.setHeader("Content-disposition",
				"attachment;filename=" + "GenericDataCollection-" + genericDataCollection.getName() + ".zip");

		ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
		addToZip("", zos, genericDataStorageFolder);
		zos.finish();
		
		// Clear security context after system operations
		SecurityContextHolder.clearContext();
	}

	//Recursive method to handle sub-folders
	public static void addToZip(String path, ZipOutputStream myZip, File f) throws FileNotFoundException, IOException{
		if(f.isDirectory()){
			for(File subF : f.listFiles()){
				addToZip(path + File.separator + f.getName() , myZip, subF);
			}
		}
		else {
			ZipEntry e = new ZipEntry(path + File.separator + f.getName());
			myZip.putNextEntry(e);
			try (InputStream is = new FileInputStream(f.getAbsolutePath())) {
				IOUtils.copyLarge(is, myZip);
			}
		}
	}
	
}
