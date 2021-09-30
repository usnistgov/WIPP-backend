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
package gov.nist.itl.ssd.wipp.backend.data.genericdata;

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
import gov.nist.itl.ssd.wipp.backend.data.utils.zip.ZipUtils;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
*/
@Controller
@RequestMapping(CoreConfig.BASE_URI + "/genericDatas/{genericDataId}/download")
public class GenericDataDownloadController {

	@Autowired
	CoreConfig config;

	@Autowired
	GenericDataRepository genericDataRepository;
	
	@Autowired
	DataDownloadTokenRepository dataDownloadTokenRepository;

	@RequestMapping(
            value = "request",
            method = RequestMethod.GET,
            produces = "application/json")
	@PreAuthorize("hasRole('admin') or @genericDataSecurity.checkAuthorize(#genericDataId, false)")
	public DownloadUrl requestDownload(
            @PathVariable("genericDataId") String genericDataId) {
    	
    	// Check existence of data
    	Optional<GenericData> gd = genericDataRepository.findById(
    			genericDataId);
        if (!gd.isPresent()) {
            throw new ResourceNotFoundException(
                    "Generic data " + genericDataId + " not found.");
        }
        
        // Generate download token
        DataDownloadToken downloadToken = new DataDownloadToken(genericDataId);
        dataDownloadTokenRepository.save(downloadToken);
        
        // Generate and send unique download URL
        String tokenParam = "?token=" + downloadToken.getToken();
        String downloadLink = linkTo(GenericDataDownloadController.class,
        		genericDataId).toString() + tokenParam;
        return new DownloadUrl(downloadLink);
    }
	
	@RequestMapping(
			value = "",
			method = RequestMethod.GET,
			produces = "application/zip")
	public void get(
			@PathVariable("genericDataId") String genericDataId,
			@RequestParam("token")String token,
			HttpServletResponse response) throws IOException {
		
    	// Load security context for system operations
    	SecurityUtils.runAsSystem();
		
    	// Check validity of download token
    	Optional<DataDownloadToken> downloadToken = dataDownloadTokenRepository.findByToken(token);
    	if (!downloadToken.isPresent() || !downloadToken.get().getDataId().equals(genericDataId)) {
    		throw new ForbiddenException("Invalid download token.");
    	}
    	
    	// Check existence of data
		GenericData genericData = null;
		Optional<GenericData> optGenericData = genericDataRepository.findById(genericDataId);
		
		if (!optGenericData.isPresent()) {
			throw new ResourceNotFoundException(
					"Generic Data " + genericDataId + " not found.");
		} else { // generic data is present
			genericData = optGenericData.get();
        }

		// get generic data folder
		File genericDataStorageFolder = new File(config.getGenericDatasFolder(), genericData.getId());
		if (! genericDataStorageFolder.exists()) {
			throw new ResourceNotFoundException(
					"Generic data " + genericDataId + " " + genericData.getName() + " not found.");
		}

		response.setHeader("Content-disposition",
				"attachment;filename=" + "GenericData-" + genericData.getName() + ".zip");

		ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
		ZipUtils.addToZip("", zos, genericDataStorageFolder);
		zos.finish();
		
		// Clear security context after system operations
		SecurityContextHolder.clearContext();
	}	
	
}
