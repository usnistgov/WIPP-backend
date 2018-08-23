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

import java.util.List;

import gov.nist.itl.ssd.wipp.backend.core.model.job.WippJob;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public abstract class WippWorkflowHandler<W extends WippWorkflow> {
	
	public abstract W createWorkflow(String name);
	
	public abstract void addJob(W workflow, WippJob job, List<String> arguments,
			List<WippNotification> notifications);
	
	public abstract void addDependency(W workflow, String child, String parent);
	
	public abstract void addWorkflowNotification(W workflow, WippNotification notification);
	
	public abstract void submitWorkflow(W workflow);
	
	public abstract void cancelWorkflow(W workflow);

}
