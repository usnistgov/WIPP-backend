package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

import java.util.Map;

public class ArgoVolume {
    private String name;
    private Map<String, String> hostPath;

    public ArgoVolume(String name, Map<String, String> hostPath) {
        this.name = name;
        this.hostPath = hostPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getHostPath() {
        return hostPath;
    }

    public void setHostPath(Map<String, String> hostPath) {
        this.hostPath = hostPath;
    }
}
