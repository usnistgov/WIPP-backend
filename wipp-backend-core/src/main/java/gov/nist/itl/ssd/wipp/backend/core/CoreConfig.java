/**
 *
 */
package gov.nist.itl.ssd.wipp.backend.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Component
public class CoreConfig {

    public static final String BASE_URI = "/api";
    public static final String PYRAMIDS_BASE_URI = "/pyramids";
    public static final int TILE_SIZE = 1024;

    @Value("${wipp.version}")
    private String wippVersion;

    @Value("${spring.data.mongodb.host}")
    private String mongodbHost;

    @Value("${spring.data.mongodb.database}")
    private String mongodbDatabase;

    @Value("${storage.root}")
    private String storageRootFolder;

    @Value("/data/inputs")
    private String containerInputsMountPath;

    @Value("/data/outputs")
    private String containerOutputsMountPath;

    @Value("${workflow.management.system:argo}")
    private String workflowManagementSystem;

    @Value("${storage.workflows}")
    private String workflowsFolder;

    @Value("${workflow.binary}")
    private String worflowBinary;

    @Value("${storage.collections}")
    private String imagesCollectionsFolder;

    @Value("${storage.stitching}")
    private String stitchingFolder;
    
    @Value("${storage.pyramids}")
    private String pyramidsFolder;

    @Value("${storage.collections.upload.tmp}")
    private String collectionsUploadTmpFolder;

    @Value("${storage.temp.jobs}")
    private String jobsTempFolder;

    @Value("${ome.converter.threads:2}")
    private int omeConverterThreads;
    
    @Value("${fetching.pixels.max}")
    private int fetchingPixelsMax;

	public String getWippVersion() {
		return wippVersion;
	}

	public String getMongodbHost() {
		return mongodbHost;
	}

	public String getMongodbDatabase() {
		return mongodbDatabase;
	}

	public String getStorageRootFolder() {
		return storageRootFolder;
	}

    public String getContainerInputsMountPath() {
	    return containerInputsMountPath;
    }

    public String getContainerOutputsMountPath() {
	    return containerOutputsMountPath;
    }

    public String getWorkflowManagementSystem() {
		return workflowManagementSystem;
	}

	public String getWorkflowsFolder() {
		return workflowsFolder;
	}

    public String getStitchingFolder() {
        return stitchingFolder;
    }
    
    public String getPyramidsFolder() {
        return pyramidsFolder;
    }

	public String getWorflowBinary() {
	    return worflowBinary;
    }

	public String getImagesCollectionsFolder() {
        return imagesCollectionsFolder;
    }

	public String getCollectionsUploadTmpFolder() {
        return collectionsUploadTmpFolder;
    }

    public String getJobsTempFolder() {
        return jobsTempFolder;
    }
    
    public int getFetchingPixelsMax() {
        return fetchingPixelsMax;
    }

    public int getOmeConverterThreads() {
        return omeConverterThreads;
    }
    
    
}
