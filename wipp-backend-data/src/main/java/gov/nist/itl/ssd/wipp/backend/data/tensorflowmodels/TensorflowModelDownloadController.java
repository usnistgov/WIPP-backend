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
package gov.nist.itl.ssd.wipp.backend.data.tensorflowmodels;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
//import io.swagger.annotations.Api;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@Controller
//@Api(tags="TensorflowModel Entity")
@RequestMapping(CoreConfig.BASE_URI + "/tensorflowModels/{tensorflowModelId}/download")
public class TensorflowModelDownloadController {

	@Autowired
	CoreConfig config;

	@Autowired
	TensorflowModelRepository tensorflowModelRepository;

	@RequestMapping(
			value = "",
			method = RequestMethod.GET,
			produces = "application/zip")
	// We make sure the user trying to download the model has the right to access it
	@PreAuthorize("@securityServiceData.checkAuthorizeTensorflowModelId(#tensorflowModelId)")
	public void get(
			@PathVariable("tensorflowModelId") String tensorflowModelId,
			HttpServletResponse response) throws IOException {
		
        TensorflowModel tm = null;
		Optional<TensorflowModel> optTm = tensorflowModelRepository.findById(tensorflowModelId);
		
		if (!optTm.isPresent()) {
			throw new ResourceNotFoundException(
					"Tensorflow model " + tensorflowModelId + " not found.");
		} else { // TrainedModel is present
            tm = optTm.get();
        }

		// get tensorflow model folder
		File tensorflowModelStorageFolder = new File(config.getTensorflowModelsFolder(), tm.getId());
		if (! tensorflowModelStorageFolder.exists()) {
			throw new ResourceNotFoundException(
					"Tensorflow model " + tensorflowModelId + " " + tm.getName() + " not found.");
		}

		response.setHeader("Content-disposition",
				"attachment;filename=" + "TensorflowModel-" + tm.getName() + ".zip");

		ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
		addToZip("", zos, tensorflowModelStorageFolder);
		zos.finish();
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
