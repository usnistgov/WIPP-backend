/**
 * 
 */
package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

import java.util.List;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class ArgoTemplateExitHandlerContainer {

	private String image;
	private List<String> args;
	
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
}
