package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

import java.util.List;
import java.util.Map;

public class ArgoTemplatePlugin extends ArgoAbstractTemplate {
    private Map<String, List<NameValueParam>> inputs;
    private ArgoTemplatePluginContainer container;

    public Map<String, List<NameValueParam>> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, List<NameValueParam>> inputs) {
        this.inputs = inputs;
    }

    public ArgoTemplatePluginContainer getContainer() {
        return container;
    }

    public void setContainer(ArgoTemplatePluginContainer container) {
        this.container = container;
    }
}
