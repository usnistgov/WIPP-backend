package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

import java.util.List;

public class ArgoWorkflowSpec {
    private static String entrypoint = "workflow";
    private List<ArgoAbstractTemplate> templates;
    private List<ArgoVolume> volumes;

    public String getEntrypoint() {
        return entrypoint;
    }

    public void setEntrypoint(String entrypoint) {
        this.entrypoint = entrypoint;
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
