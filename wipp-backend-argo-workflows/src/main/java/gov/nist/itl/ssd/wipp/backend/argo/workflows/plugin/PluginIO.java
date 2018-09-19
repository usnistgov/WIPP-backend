package gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin;

import java.util.Map;

/**
 *
 * @author Philippe Dessauw <philippe.dessauw at nist.gov>
 */
public class PluginIO {
    private String name;
    private String description;
    private String type;
    // The options field contains any objects and is parsed by the UI
    private Map<String, Object> options;
    private boolean required = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setFormat(Map<String, Object> options) {
        this.options = options;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
