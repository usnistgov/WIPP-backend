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
package gov.nist.itl.ssd.wipp.backend.data.utils.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Util methods for Zipping files/directories
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class ZipUtils {
	
	/**
	 * Recursively add content of file or directory to ZipOutputStream
	 * @param path Path in the Zip structure
	 * @param zipOut ZipOutputStream
	 * @param f File (file or directory) to add to Zip
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void addToZip(String path, ZipOutputStream zipOut, File file) throws FileNotFoundException, IOException {
		if (file.isDirectory()) {
			for (File subF : file.listFiles()) {
				addToZip(path + File.separator + file.getName(), zipOut, subF);
			}
		} else {
			ZipEntry zipEntry = new ZipEntry(path + File.separator + file.getName());
			zipOut.putNextEntry(zipEntry);
			try (InputStream is = new FileInputStream(file.getAbsolutePath())) {
				IOUtils.copyLarge(is, zipOut);
			}
		}
	}
}
