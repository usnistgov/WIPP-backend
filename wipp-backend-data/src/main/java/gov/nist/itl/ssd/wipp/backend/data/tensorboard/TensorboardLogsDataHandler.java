package gov.nist.itl.ssd.wipp.backend.data.tensorboard;

import java.io.File;
import java.util.Optional;

import gov.nist.itl.ssd.wipp.backend.core.model.data.BaseDataHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobExecutionException;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
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
		// Set owner to job owner
		tl.setOwner(job.getOwner());
		// Set TL to private
		tl.setPubliclyShared(false);
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
	
	@Override
    public void setDataToPublic(String value) {
    	Optional<TensorboardLogs> optTensorboardLogs = tensorboardLogsRepository.findById(value);
        if(optTensorboardLogs.isPresent()) {
        	TensorboardLogs tensorboardLogs = optTensorboardLogs.get();
            if (!tensorboardLogs.isPubliclyShared()) {
            	tensorboardLogs.setPubliclyShared(true);
            	tensorboardLogsRepository.save(tensorboardLogs);
            }
        }
    }
	
}
