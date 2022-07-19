/*
 * This software was developed at the National Institute of Standards and
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
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
