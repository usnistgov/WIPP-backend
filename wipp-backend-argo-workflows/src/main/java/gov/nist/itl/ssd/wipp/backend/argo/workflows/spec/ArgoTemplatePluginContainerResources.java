package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

import gov.nist.itl.ssd.wipp.backend.core.model.computation.PluginResourceRequirements;

import java.util.HashMap;
import java.util.Map;

public class ArgoTemplatePluginContainerResources {

    private Map<String, String> requests;

    private Map<String, String> limits;

    public ArgoTemplatePluginContainerResources(PluginResourceRequirements resourceRequirements) {
        if (resourceRequirements != null) {
            this.generateContainerResourceRequests(resourceRequirements);
            this.generateContainerResourceLimits(resourceRequirements);
        }
    }

    public Map<String, String> getRequests() {
        return requests;
    }

    public void setRequests(Map<String, String> requests) {
        this.requests = requests;
    }

    public Map<String, String> getLimits() {
        return limits;
    }

    public void setLimits(Map<String, String> limits) {
        this.limits = limits;
    }

    private void generateContainerResourceRequests(PluginResourceRequirements pluginResourceRequirements) {
        Map<String, String> requests = new HashMap<>();

        if (pluginResourceRequirements.getCoresMin() != null) {
            requests.put("cpu", String.valueOf(pluginResourceRequirements.getCoresMin()));
        }
        if (pluginResourceRequirements.getRamMin() != null) {
            requests.put("memory", String.valueOf(pluginResourceRequirements.getRamMin()) + "Mi");
        }

        if(!requests.isEmpty()) {
            this.requests = requests;
        }
    }

    private void generateContainerResourceLimits(PluginResourceRequirements pluginResourceRequirements) {
        Map<String, String> limits = new HashMap<>();

        if (pluginResourceRequirements.isGpu()) {
            limits.put("nvidia.com/gpu", "1");
        }

        if (!limits.isEmpty()) {
            this.limits = limits;
        }
    }

}
