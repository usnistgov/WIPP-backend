package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class ArgoTemplatePluginSlurm extends ArgoAbstractTemplate {
    private ArgoTemplatePluginSlurmResource resource;


    public ArgoTemplatePluginSlurmResource getResource() {
        return resource;
    }

    public void setResource(ArgoTemplatePluginSlurmResource resource) {
        this.resource = resource;
    }
}
