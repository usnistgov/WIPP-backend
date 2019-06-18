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
package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

/**
 * Argo PersistentVolumeClaim spec
 *  
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class ArgoPersistentVolumeClaim {
	
	private String claimName;
	
	public ArgoPersistentVolumeClaim(String claimName) {
		this.claimName = claimName;
	}

	public String getClaimName() {
		return claimName;
	}

	public void setClaimName(String claimName) {
		this.claimName = claimName;
	}
}
