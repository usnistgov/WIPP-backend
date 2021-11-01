package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

import gov.nist.itl.ssd.wipp.backend.argo.workflows.spec.slurmjob.ArgoTemplatePluginSlurmJob;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class ArgoTemplatePluginSlurmResource {

	private final String action = "create";
    private final String successCondition = "Status.Status == Succeeded";
    private final String failureCondition = "Status.Status == Failed";
    private String manifest;
	
    public String getAction() {
		return action;
	}

	public String getSuccessCondition() {
		return successCondition;
	}

	public String getFailureCondition() {
		return failureCondition;
	}

	public String getManifest() {
		return manifest;
	}

	public void setManifest(String manifest) {
		this.manifest = manifest;
	}
    
}
