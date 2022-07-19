package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

import gov.nist.itl.ssd.wipp.backend.core.model.computation.PluginResourceRequirements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgoTemplatePlugin extends ArgoAbstractTemplate {
    private Map<String, List<NameValueParam>> inputs;
    private ArgoTemplatePluginContainer container;

    private Map<String, String> nodeSelector;

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

    public Map<String, String> getNodeSelector() {
        return nodeSelector;
    }

    public void setNodeSelector(Map<String, String> nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

    public void setNodeSelector(PluginResourceRequirements pluginResourceRequirements) {
        if (pluginResourceRequirements != null) {
            Map<String, String> selectors = new HashMap<>();
            // add CPU-related selectors
            if (pluginResourceRequirements.isCpuAVX()) {
                selectors.put("feature.node.kubernetes.io/cpu-cpuid.AVX", "true");
            }
            if (pluginResourceRequirements.isCpuAVX2()) {
                selectors.put("feature.node.kubernetes.io/cpu-cpuid.AVX2", "true");
            }

            if (!selectors.isEmpty()) {
                this.nodeSelector = selectors;
            }
        }
    }
}
