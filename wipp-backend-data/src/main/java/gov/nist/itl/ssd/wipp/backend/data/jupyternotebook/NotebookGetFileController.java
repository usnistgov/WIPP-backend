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
package gov.nist.itl.ssd.wipp.backend.data.jupyternotebook;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
//import io.swagger.annotations.Api;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
*/
@Controller
//@Api(tags="Notebook Entity")
@RequestMapping(CoreConfig.BASE_URI + "/notebooks/{notebookId}/getFile")
public class NotebookGetFileController {

	@Autowired
	CoreConfig config;
	
	@Autowired
	private NotebookRepository notebookRepository;
	
	@RequestMapping(
			value = "",
			method = RequestMethod.GET,
			produces = "application/json")
	public void get(
			@PathVariable("notebookId") String notebookId,
			HttpServletResponse response) throws IOException {
		
        Notebook notebook = null;
		Optional<Notebook> optNotebook = notebookRepository.findById(notebookId);
		
		if (!optNotebook.isPresent()) {
			throw new ResourceNotFoundException(
					"Notebook " + notebookId + " not found.");
		} else { 
			notebook = optNotebook.get();
        }

		File notebookStorageFolder = new File(config.getNotebooksFolder(), notebook.getId());
		File notebookJsonFile = new File (notebookStorageFolder, NotebookConfig.NOTEBOOK_FILENAME);
			
		if (! notebookStorageFolder.exists()) {
			throw new ResourceNotFoundException(
					"Notebook " + notebookId + " " + notebook.getName() + " not found.");
		}

		response.setHeader("Content-disposition",
				"attachment;filename=" + "Notebook-" + notebookJsonFile);
		
        try (InputStream is = new BufferedInputStream(
                new FileInputStream(notebookJsonFile))) {
            IOUtils.copy(is, response.getOutputStream());
        }
        response.flushBuffer();
	}
	
}
