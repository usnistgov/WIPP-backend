package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

import java.util.List;
import java.util.Map;

public class ArgoTemplatePluginContainer {
    private String image;
    private List<String> args;
    private List<Map<String, String>> volumeMounts;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public List<Map<String, String>> getVolumeMounts() {
        return volumeMounts;
    }

    public void setVolumeMounts(List<Map<String, String>> volumeMounts) {
        this.volumeMounts = volumeMounts;
    }
}
