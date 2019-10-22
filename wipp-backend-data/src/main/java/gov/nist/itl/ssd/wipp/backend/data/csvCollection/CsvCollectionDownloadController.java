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
package gov.nist.itl.ssd.wipp.backend.data.csvCollection;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import io.swagger.annotations.Api;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
*/
@Controller
@Api(tags="CsvCollection Entity")
@RequestMapping(CoreConfig.BASE_URI + "/csvCollections/{csvCollectionId}/download")
public class CsvCollectionDownloadController {

	@Autowired
	CoreConfig config;

	@Autowired
	CsvCollectionRepository csvCollectionRepository;

	@RequestMapping(
			value = "",
			method = RequestMethod.GET,
			produces = "application/zip")
	public void get(
			@PathVariable("csvCollectionId") String csvCollectionId,
			HttpServletResponse response) throws IOException {
		
        CsvCollection csvCollection = null;
		Optional<CsvCollection> optCsvCollection = csvCollectionRepository.findById(csvCollectionId);
		
		if (!optCsvCollection.isPresent()) {
			throw new ResourceNotFoundException(
					"Csv Collection " + csvCollectionId + " not found.");
		} else { // csv collection is present
			csvCollection = optCsvCollection.get();
        }

		// get csv collection folder
		File csvCollectionStorageFolder = new File(config.getCsvCollectionsFolder(), csvCollection.getId());
		if (! csvCollectionStorageFolder.exists()) {
			throw new ResourceNotFoundException(
					"Csv Collection " + csvCollectionId + " " + csvCollection.getName() + " not found.");
		}

		response.setHeader("Content-disposition",
				"attachment;filename=" + "CsvCollection-" + csvCollection.getName() + ".zip");

		ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
		addToZip("", zos, csvCollectionStorageFolder);
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
