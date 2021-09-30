package gov.nist.itl.ssd.wipp.backend.data.imagescollection.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;

public abstract class FileUploadBase {

	@Autowired
	private CoreConfig config;


	protected abstract String getUploadSubFolder();

	protected File getUploadDir(String imagesCollectionId) {
		return new File(
				new File(config.getImagesCollectionsFolder(), imagesCollectionId),
				getUploadSubFolder());
	}

	protected File getTempUploadDir(String imagesCollectionId) {
		return new File(new File(
				config.getCollectionsUploadTmpFolder(), imagesCollectionId),
				getUploadSubFolder());
	}
	
	protected static long getPathSize(Path path) {
		long size = 0;
		try {
			size = FileUtils.sizeOf(path.toFile());
		} catch (UnsupportedOperationException O_o) {
			// Safely ignore, the size will just be 0.
		}
		return size;
	}

}
