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
package gov.nist.itl.ssd.wipp.backend.data.visualization.manifest.layers;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class Layer {
	
	@Field("id")
	private String id;
	
	private String name;
	
	//@ManualRef(Pyramid.class)
	@JsonDeserialize(using = BaseUrlManualRefDeserializer.class)
	@JsonSerialize(using = BaseUrlManualRefSerializer.class)
	private String baseUrl;
	
	private boolean singleFrame;
	
	private String framesPrefix;
	
	private String framesSuffix;
	
	private int openOnFrame;
	
	private int numberOfFrames;
	
	private int framesOffset;
	
	private int[] framesList;
	
	private int paddingSize;
	
	private AcquiredIntensity acquiredIntensity;
	
	private Scalebar scalebar;
	
	private Fetching fetching;

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

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public boolean isSingleFrame() {
		return singleFrame;
	}

	public void setSingleFrame(boolean singleFrame) {
		this.singleFrame = singleFrame;
	}

	public String getFramesPrefix() {
		return framesPrefix;
	}

	public void setFramesPrefix(String framesPrefix) {
		this.framesPrefix = framesPrefix;
	}

	public String getFramesSuffix() {
		return framesSuffix;
	}

	public void setFramesSuffix(String framesSuffix) {
		this.framesSuffix = framesSuffix;
	}

	public int getOpenOnFrame() {
		return openOnFrame;
	}

	public void setOpenOnFrame(int openOnFrame) {
		this.openOnFrame = openOnFrame;
	}

	public int getNumberOfFrames() {
		return numberOfFrames;
	}

	public void setNumberOfFrames(int numberOfFrames) {
		this.numberOfFrames = numberOfFrames;
	}

	public int getFramesOffset() {
		return framesOffset;
	}

	public void setFramesOffset(int framesOffset) {
		this.framesOffset = framesOffset;
	}

	public int[] getFramesList() {
		return framesList;
	}

	public void setFramesList(int[] framesList) {
		this.framesList = framesList;
	}

	public int getPaddingSize() {
		return paddingSize;
	}

	public void setPaddingSize(int paddingSize) {
		this.paddingSize = paddingSize;
	}

	public AcquiredIntensity getAcquiredIntensity() {
		return acquiredIntensity;
	}

	public void setAcquiredIntensity(AcquiredIntensity acquiredIntensity) {
		this.acquiredIntensity = acquiredIntensity;
	}

	public Scalebar getScalebar() {
		return scalebar;
	}

	public void setScalebar(Scalebar scalebar) {
		this.scalebar = scalebar;
	}

	public Fetching getFetching() {
		return fetching;
	}

	public void setFetching(Fetching fetching) {
		this.fetching = fetching;
	}
	
	

}
