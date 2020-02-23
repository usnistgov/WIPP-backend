package gov.nist.itl.ssd.wipp.backend.data.tensorboard;

import java.io.File;

import gov.nist.itl.ssd.wipp.backend.core.model.data.BaseDataHandler;
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
public class TensorboardLogsDataHandler extends BaseDataHandler implements DataHandler{

	
    @Autowired
    CoreConfig config;
    
    @Autowired
    private TensorboardLogsRepository tensorboardLogsRepository;

	@Override
	public void importData(Job job, String outputName) throws JobExecutionException {
		
		TensorboardLogs tl = new TensorboardLogs(job, outputName);
		// When a tensorboard logs is created as a result of a Job, the tensorboard logs' owner will correspond to the Job's owner and the tensorboard logs' availability will be set to private by default
		tl.setOwner(job.getOwner());
		//TODO : set also the isPubliclyAvailable attribute here
		//tl.setPubliclyAvailable(job.isPubliclyAvailable());
		tl.setPubliclyAvailable(false);
		tensorboardLogsRepository.save(tl);
		
		File tensorboardLogsFolder = new File(config.getTensorboardLogsFolder(), tl.getName());
		tensorboardLogsFolder.mkdirs();
		
		File tempOutputDir = getJobOutputTempFolder(job.getId(), outputName);
		boolean success = tempOutputDir.renameTo(tensorboardLogsFolder);
		if (!success) {
			tensorboardLogsRepository.delete(tl);
			throw new JobExecutionException("Cannot move tensorboard logs to final destination.");
		}
		setOutputId(job, outputName, tl.getId());
	}

	@Override
	public String exportDataAsParam(String value) {
		String tensorboardLogsId = value;
		File inputTensorboardLogs = new File(config.getTensorflowModelsFolder(), tensorboardLogsId);
		String tensorboardLogsPath = inputTensorboardLogs.getAbsolutePath();
		return tensorboardLogsPath;
	}
	
}
