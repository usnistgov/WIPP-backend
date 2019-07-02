/*
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
 */
package gov.nist.itl.ssd.wipp.backend.data.visualization.manifest;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import gov.nist.itl.ssd.wipp.backend.data.visualization.manifest.layers.Layer;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class LayersGroup {

	@Field("id")
	private String id;
	
	private String name;
	
	private List<Layer> layers;

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

	public List<Layer> getLayers() {
		return layers;
	}

	public void setLayers(List<Layer> layers) {
		this.layers = layers;
	}
}
