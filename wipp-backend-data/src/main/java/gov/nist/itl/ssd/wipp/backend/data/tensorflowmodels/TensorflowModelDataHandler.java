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

import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
@Component("tensorflowModelDataHandler")

public class TensorflowModelDataHandler extends BaseDataHandler implements DataHandler {

	@Autowired
	CoreConfig config;

	@Autowired
	private TensorflowModelRepository tensorflowModelRepository;

	@Override
	public void importData(Job job, String outputName) throws JobExecutionException {
		TensorflowModel tm = new TensorflowModel(job, outputName);
		// Set owner to job owner
        tm.setOwner(job.getOwner());
        // Set TM to private
        tm.setPubliclyShared(false);
        tensorflowModelRepository.save(tm);

		File trainedModelFolder = new File(config.getTensorflowModelsFolder(), tm.getId());
		trainedModelFolder.mkdirs();

		File tempOutputDir = getJobOutputTempFolder(job.getId(), outputName);
		boolean success = tempOutputDir.renameTo(trainedModelFolder);
		if (!success) {
			tensorflowModelRepository.delete(tm);
			throw new JobExecutionException("Cannot move tensorflow model to final destination.");
		}

		setOutputId(job, outputName, tm.getId());
	}
	
	@Override
    public String exportDataAsParam(String value) {
        String tensorflowModelId = value;
        String tensorflowModelPath;

        // check if the input of the job is the output of another job and if so return the associated path
        String regex = "\\{\\{ (.*)\\.(.*) \\}\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(tensorflowModelId);
        if (m.find()) {
            String jobId = m.group(1);
            String outputName = m.group(2);
            tensorflowModelPath = getJobOutputTempFolder(jobId, outputName).getAbsolutePath();
        }
        // else return the path of the tensorflow model
        else {
            File tensorflowModelFolder = new File(config.getTensorflowModelsFolder(), tensorflowModelId);
            tensorflowModelPath = tensorflowModelFolder.getAbsolutePath();

        }
        tensorflowModelPath = tensorflowModelPath.replaceFirst(config.getStorageRootFolder(),config.getContainerInputsMountPath());
        return tensorflowModelPath;

    }
	
	@Override
    public void setDataToPublic(String value) {
    	Optional<TensorflowModel> optTensorflowModel = tensorflowModelRepository.findById(value);
        if(optTensorflowModel.isPresent()) {
        	TensorflowModel tensorflowModel = optTensorflowModel.get();
            if (!tensorflowModel.isPubliclyShared()) {
            	tensorflowModel.setPubliclyShared(true);
            	tensorflowModelRepository.save(tensorflowModel);
            }
        }
    }

}
