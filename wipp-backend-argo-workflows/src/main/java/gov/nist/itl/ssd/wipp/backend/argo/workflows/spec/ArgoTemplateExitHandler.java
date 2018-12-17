package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class ArgoTemplateExitHandler extends ArgoAbstractTemplate {

	private ArgoTemplateExitHandlerContainer container;

	public ArgoTemplateExitHandlerContainer getContainer() {
		return container;
	}

	public void setContainer(ArgoTemplateExitHandlerContainer container) {
		this.container = container;
	}
	
}
