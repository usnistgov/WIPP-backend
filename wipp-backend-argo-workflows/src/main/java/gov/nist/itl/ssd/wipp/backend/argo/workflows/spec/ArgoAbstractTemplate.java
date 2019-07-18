package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

public abstract class ArgoAbstractTemplate {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
