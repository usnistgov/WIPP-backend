/**
 * 
 */
package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec.slurmjob;

import java.util.Map;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class ArgoTemplatePluginSlurmJob {

	private final String apiVersion = "wlm.sylabs.io/v1alpha1";
    private final String kind = "SlurmJob";
    private Map<String, String> metadata;
    private ArgoTemplatePluginSlurmJobSpec spec;
    
	public String getApiVersion() {
		return apiVersion;
	}

	public String getKind() {
		return kind;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public ArgoTemplatePluginSlurmJobSpec getSpec() {
		return spec;
	}

	public void setSpec(ArgoTemplatePluginSlurmJobSpec spec) {
		this.spec = spec;
	}
}
