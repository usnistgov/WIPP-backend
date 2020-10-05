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
package gov.nist.itl.ssd.wipp.backend.argo.workflows.job;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin.Plugin;
import gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin.PluginIO;
import gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin.PluginRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandlerService;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobUtilsService;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Primary
@Service
public class ArgoJobUtilsService implements JobUtilsService {

	@Autowired
    private PluginRepository wippPluginRepository;
	
	@Autowired
    private DataHandlerService dataHandlerService;
	
	/* (non-Javadoc)
	 * @see gov.nist.itl.ssd.wipp.backend.core.model.job.JobUtilsService#setOutputsToPublic(gov.nist.itl.ssd.wipp.backend.core.model.job.Job)
	 */
	@Override
	public void setOutputsToPublic(Job job) {
		Optional<Plugin> pluginOpt = wippPluginRepository.findById(job.getWippExecutable());
        Plugin plugin = pluginOpt.get();
        List<PluginIO> outputs = plugin.getOutputs();
        for (PluginIO output : outputs) {
            DataHandler dataHandler = dataHandlerService.getDataHandler(output.getType());
            dataHandler.setDataToPublic(job.getOutputParameter(output.getName()));
        }
	}

}
