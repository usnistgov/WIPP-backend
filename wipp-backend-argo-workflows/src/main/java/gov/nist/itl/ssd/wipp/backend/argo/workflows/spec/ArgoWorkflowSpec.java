package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

import java.util.List;

/**
*
* @author Philippe Dessauw <philippe.dessauw at nist.gov>
* @author Mylene Simon <mylene.simon at nist.gov>
* 
*/
public class ArgoWorkflowSpec {
    private final String entrypoint = "workflow";
    private final String onExit = "exit-handler";
    private List<ArgoAbstractTemplate> templates;
    private List<ArgoVolume> volumes;

    public String getEntrypoint() {
        return entrypoint;
    }

    public String getOnExit() {
		return onExit;
	}

	public List<ArgoAbstractTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<ArgoAbstractTemplate> templates) {
        this.templates = templates;
    }

    public List<ArgoVolume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<ArgoVolume> volumes) {
        this.volumes = volumes;
    }
}
