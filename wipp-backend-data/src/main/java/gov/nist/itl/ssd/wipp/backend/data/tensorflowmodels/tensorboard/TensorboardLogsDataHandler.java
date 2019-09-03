package gov.nist.itl.ssd.wipp.backend.data.tensorflowmodels.tensorboard;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobExecutionException;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@Component("tensorboardLogsDataHandler")
public class TensorboardLogsDataHandler  implements DataHandler{

	
    @Autowired
    CoreConfig config;
    
    @Autowired
    private TensorboardLogsRepository tensorboardLogsRepository;

	@Override
	public void importData(Job job, String outputName) throws JobExecutionException {
		
		TensorboardLogs tl = new TensorboardLogs(job, outputName);
		tensorboardLogsRepository.save(tl);
		
		File tensorboardLogsFolder = new File(config.getTensorboardLogsFolder(), tl.getName());
		tensorboardLogsFolder.mkdirs();
		
		File tempOutputDir = getJobOutputTempFolder(job, outputName);
		boolean success = tempOutputDir.renameTo(tensorboardLogsFolder);
		if (!success) {
			tensorboardLogsRepository.delete(tl);
			throw new JobExecutionException("Cannot move tensorboard logs to final destination.");
		}		
	}

	@Override
	public String exportDataAsParam(String value) {
		String tensorboardLogsId = value;
		File inputTensorboardLogs = new File(config.getTensorflowModelsFolder(), tensorboardLogsId);
		String tensorboardLogsPath = inputTensorboardLogs.getAbsolutePath();
		return tensorboardLogsPath;
	}
	
    private final File getJobOutputTempFolder(Job job, String outputName) {
        return new File( new File(config.getJobsTempFolder(), job.getId()), outputName);
    }
}
