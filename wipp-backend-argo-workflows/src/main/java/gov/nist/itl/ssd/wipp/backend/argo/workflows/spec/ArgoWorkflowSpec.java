package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

import java.util.List;
import java.util.Map;

/**
*
* @author Philippe Dessauw <philippe.dessauw at nist.gov>
* @author Mylene Simon <mylene.simon at nist.gov>
* 
*/
public class ArgoWorkflowSpec {
    private final String entrypoint = "workflow";
    private final String onExit = "exit-handler";
    private Map<String, String> nodeSelector;
    private List<Map<String, String>> tolerations;
    private List<ArgoAbstractTemplate> templates;
    private List<ArgoVolume> volumes;

    public String getEntrypoint() {
        return entrypoint;
    }

    public String getOnExit() {
		return onExit;
	}

    public Map<String, String> getNodeSelector() {
        return nodeSelector;
    }

    public void setNodeSelector(Map<String, String> node) {
        this.nodeSelector = node;
    }

    public List<Map<String, String>> getTolerations() {
        return tolerations;
    }

    public void setTolerations(List<Map<String, String>> tolerations) {
        this.tolerations = tolerations;
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
