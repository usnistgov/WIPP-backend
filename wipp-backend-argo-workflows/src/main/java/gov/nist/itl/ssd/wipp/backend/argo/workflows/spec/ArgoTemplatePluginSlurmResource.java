package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class ArgoTemplatePluginSlurmResource {

	private final String action = "create";
    private final String successCondition = "status.status == Succeeded";
    private final String failureCondition = "status.status in (Failed, Error)";
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
