package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

import java.util.List;
import java.util.Map;

public class ArgoWorkflow {
    private static String apiVersion = "argoproj.io/v1alpha1";
    private static String kind = "workflow";
    private Map<String, String> metadata;
    private ArgoWorkflowSpec spec;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public ArgoWorkflowSpec getSpec() {
        return spec;
    }

    public void setSpec(ArgoWorkflowSpec spec) {
        this.spec = spec;
    }
}
