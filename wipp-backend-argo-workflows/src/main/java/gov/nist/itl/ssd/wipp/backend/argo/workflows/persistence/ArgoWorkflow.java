package gov.nist.itl.ssd.wipp.backend.argo.workflows.persistence;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "argo_workflows")
public class ArgoWorkflow {

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    private String name;

    private String phase;

    private String namespace;

    private String workflow;

    private Date startedat;

    private Date finishedat;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public Date getStartedat() {
        return startedat;
    }

    public void setStartedat(Date startedat) {
        this.startedat = startedat;
    }

    public Date getFinishedat() {
        return finishedat;
    }

    public void setFinishedat(Date finishedat) {
        this.finishedat = finishedat;
    }
}
