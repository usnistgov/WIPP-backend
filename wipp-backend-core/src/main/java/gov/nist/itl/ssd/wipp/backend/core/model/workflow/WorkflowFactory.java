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
package gov.nist.itl.ssd.wipp.backend.core.model.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Service
public class WorkflowFactory {

	@Autowired
	private CoreConfig config;
	
	@Autowired
    private ApplicationContext context;

//	public WorkflowHandler<? extends Workflow> getWorkflowHandler() {
//
//		String workflowType = config.getWorkflowManagementSystem();
//
//		if(workflowType.equals("pegasus"))
//			return context.getBean(PegasusWippWorkflowHandler.class);
//
//		else
//			throw new InternalError("Unknown Wipp Workflow type " + workflowType);
//
//	}
}
