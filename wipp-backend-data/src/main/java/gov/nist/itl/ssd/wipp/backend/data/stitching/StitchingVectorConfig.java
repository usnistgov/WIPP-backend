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
package gov.nist.itl.ssd.wipp.backend.data.stitching;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
public class StitchingVectorConfig {

	private static final String STITCHING_VECTOR_FILENAME_PREFIX = "img-";
	public static final String STITCHING_VECTOR_FILENAME_SUFFIX = ".txt";
	public static final String STITCHING_VECTOR_GLOBAL_POSITION_PREFIX = STITCHING_VECTOR_FILENAME_PREFIX + "global-positions-";
	public static final String STITCHING_VECTOR_STATISTICS_FILE_NAME = STITCHING_VECTOR_FILENAME_PREFIX + "statistics" 
			+ STITCHING_VECTOR_FILENAME_SUFFIX;
}
