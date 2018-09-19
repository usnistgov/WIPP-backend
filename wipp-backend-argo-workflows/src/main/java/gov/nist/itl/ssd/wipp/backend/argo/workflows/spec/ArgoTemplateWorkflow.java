package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgoTemplateWorkflow extends ArgoAbstractTemplate {
    private Map<String, List<ArgoTemplateWorkflowTask>> dag;

    public ArgoTemplateWorkflow(List<ArgoTemplateWorkflowTask> tasks) {
        super();

        this.setName("workflow");

        this.dag = new HashMap<>();
        this.dag.put("tasks", tasks);
    }

    public Map<String, List<ArgoTemplateWorkflowTask>> getDag() {
        return dag;
    }

    public void setDag(Map<String, List<ArgoTemplateWorkflowTask>> dag) {
        this.dag = dag;
    }
}
