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

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class WorkflowNotification {

	public enum WHEN {
		START,
		SUCCESS,
		ERROR,
		END
	}
	
	private WHEN event;
	
	private String command;
	
	public WorkflowNotification(WHEN event, String command) {
		this.setEvent(event);
		this.setCommand(command);
	}

	public WHEN getEvent() {
		return event;
	}

	public void setEvent(WHEN event) {
		this.event = event;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
}
