/**
 * NIST-developed software is provided by NIST as a public service. You may
 * use, copy, and distribute copies of the software in any medium, provided
 * that you keep intact this entire notice. You may improve, modify, and create
 * derivative works of the software or any portion of the software, and you may
 * copy and distribute such modifications or works. Modified works should carry
 * a notice stating that you changed the software and should note the date and
 * nature of any such change. Please explicitly acknowledge the National
 * Institute of Standards and Technology as the source of the software.
 *
 * NIST-developed software is expressly provided "AS IS." NIST MAKES NO
 * WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT, OR ARISING BY OPERATION OF
 * LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT, AND DATA ACCURACY. NIST
 * NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE
 * UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST
 * DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE
 * SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE
 * CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.
 *
 * You are solely responsible for determining the appropriateness of using and
 * distributing the software and you assume all risks associated with its use,
 * including but not limited to the risks and costs of program errors,
 * compliance with applicable laws, damage to or loss of data, programs or
 * equipment, and the unavailability or interruption of operation. This
 * software is not intended to be used in any situation where a failure could
 * cause risk of injury or damage to property. The software developed by NIST
 * employees is not subject to copyright protection within the United States.
 */
package gov.nist.itl.ssd.wipp.backend.core.model.computation;

import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;

import java.util.List;

/**
 *
 * @author Philippe Dessauw <philippe.dessauw at nist.gov>
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@IdExposed
public class Plugin extends Computation {
    
    private String containerId;
    private List<String> baseCommand;

    private List<String> operationType;
    private String title;
    private String description;
    private String author;
    private String institution;
    private String repository;
    private String website;
    private String citation;

    private List<PluginIO> inputs;
    private List<PluginIO> outputs;

    private PluginResourceRequirements resourceRequirements;

    private List<Object> ui;  // TODO describe parameter so as not to use Object

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public List<String> getBaseCommand() {
        return baseCommand;
    }

    public void setBaseCommand(List<String> baseCommand) {
        this.baseCommand = baseCommand;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getCitation() {
		return citation;
	}

	public void setCitation(String citation) {
		this.citation = citation;
	}

    public List<String> getOperationType() { return operationType; }

    public void setOperationType(List<String> operationType) { this.operationType = operationType; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PluginIO> getInputs() {
        return inputs;
    }

    public void setInputs(List<PluginIO> inputs) {
        this.inputs = inputs;
    }

    public List<PluginIO> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<PluginIO> outputs) {
        this.outputs = outputs;
    }

    public PluginResourceRequirements getResourceRequirements() {
        return resourceRequirements;
    }

    public void setResourceRequirements(PluginResourceRequirements resourceRequirements) {
        this.resourceRequirements = resourceRequirements;
    }

    public List<Object> getUi() {
        return ui;
    }

    public void setUi(List<Object> ui) {
        this.ui = ui;
    }
}
