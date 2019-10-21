package gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin;

import gov.nist.itl.ssd.wipp.backend.core.model.computation.Computation;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;

import java.util.List;

/**
 *
 * @author Philippe Dessauw <philippe.dessauw at nist.gov>
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@IdExposed
public class Plugin extends Computation {
    private String containerId;

    private String title;
    private String description;
    private String author;
    private String institution;
    private String repository;
    private String website;
    private String citation;    

    private List<PluginIO> inputs;
    private List<PluginIO> outputs;
    private List<Object> ui;  // TODO describe parameter so as not to use Object

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
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

    public List<Object> getUi() {
        return ui;
    }

    public void setUi(List<Object> ui) {
        this.ui = ui;
    }
}
