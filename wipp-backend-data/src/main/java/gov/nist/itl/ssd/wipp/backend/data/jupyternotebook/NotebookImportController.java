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

import java.io.File;
import java.io.IOException;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
*/
@RestController
@Tag(name="Notebook Entity")
@RequestMapping(CoreConfig.BASE_URI + "/notebooks/import")
public class NotebookImportController {

	@Autowired
	CoreConfig config;
	
	@Autowired
	private NotebookRepository notebookRepository;
	
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    @PreAuthorize("isAuthenticated()")
    public Notebook importNb(
    		@RequestParam("folderName") String folderName,
            @RequestParam("name") String name,
            @RequestParam(value = "description",
                    required = false) String description)
            throws IOException {
        if (name == null || name.isEmpty()) {
            throw new ClientException(
                    "A notebook name must be specified.");
        }
        
        if(notebookRepository.findOneByName(name) != null){
        	throw new ClientException(
                    "A notebook with this name already exists.");
        }
        
        Notebook notebook = new Notebook(name, description);
        notebook = notebookRepository.save(notebook);
        
        // Notebook temp folder
        File notebookTempFolder = new File (
        		config.getNotebooksTmpFolder(),
        		folderName);
        File notebookTmpFile = new File (notebookTempFolder, NotebookConfig.NOTEBOOK_FILENAME);
        
        // Notebook wipp folder
        File notebookFolder = new File (
        		config.getNotebooksFolder(),
        		notebook.getId());
        File notebookFile = new File(notebookFolder,  NotebookConfig.NOTEBOOK_FILENAME);
        
        notebookFolder.mkdirs();
        
        // move temp file to wipp location
        boolean success = notebookTmpFile.renameTo(notebookFile);
		if (!success) {
			notebookRepository.delete(notebook);
			notebookFolder.delete();
			throw new ClientException("Cannot move notebook to final destination.");
		}
        
        return notebook;
    }
}
