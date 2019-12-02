package gov.nist.itl.ssd.wipp.backend.data.pyramid.annotations;

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
@Component("pyramidAnnotationDataHandler")
public class PyramidAnnotationDataHandler extends BaseDataHandler implements DataHandler{

    @Autowired
    CoreConfig config;

    public PyramidAnnotationDataHandler() {
    }

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
