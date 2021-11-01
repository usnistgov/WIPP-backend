package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

import java.util.List;
import java.util.Map;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class ArgoTemplatePluginSlurm extends ArgoAbstractTemplate {
    private Map<String, List<NameValueParam>> inputs;
    private ArgoTemplatePluginSlurmResource resource;

    public Map<String, List<NameValueParam>> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, List<NameValueParam>> inputs) {
        this.inputs = inputs;
    }

    public ArgoTemplatePluginSlurmResource getResource() {
        return resource;
    }

    public void setResource(ArgoTemplatePluginSlurmResource resource) {
        this.resource = resource;
    }
}
