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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.BaseDataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@Component("notebookDataHandler")
public class NotebookDataHandler extends BaseDataHandler implements DataHandler{
	
	@Autowired
	CoreConfig config;
	
	
	@Override
	public void importData(Job job, String outputName) throws Exception {
		throw new RuntimeException("import of notebooks from jobs is not implemented");	
	}

	@Override
	public String exportDataAsParam(String value) {
        String notebookId = value;
        String notebookPath;

        // check if the input of the job is the output of another job and if so return the associated path
        String regex = "\\{\\{ (.*)\\.(.*) \\}\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(notebookId);
        if (m.find()) {
            String jobId = m.group(1);
            String outputName = m.group(2);
            notebookPath = getJobOutputTempFolder(jobId, outputName).getAbsolutePath();
        }
        // else return the path of the notebook
        else {
            File notebookFolder = new File(config.getNotebooksFolder(), notebookId);
            notebookPath = notebookFolder.getAbsolutePath();

        }
        notebookPath = notebookPath.replaceFirst(config.getStorageRootFolder(),config.getContainerInputsMountPath());
        return notebookPath;
	}

}
