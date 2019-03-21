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
    
    private static final String STITCHING_VECTOR_FILENAME_PREFIX = "img-";
    public static final String STITCHING_VECTOR_FILENAME_SUFFIX = ".txt";
    public static final String STITCHING_VECTOR_GLOBAL_POSITION_PREFIX
            = STITCHING_VECTOR_FILENAME_PREFIX + "global-positions-";
    public static final String STITCHING_VECTOR_STATISTICS_FILE_NAME
            = STITCHING_VECTOR_FILENAME_PREFIX + "statistics"
            + STITCHING_VECTOR_FILENAME_SUFFIX;


    @Value("${wipp.version}")
    private String wippVersion;
    
    @Value("${spring.data.mongodb.host}")
    private String mongodbHost;
    
    @Value("${spring.data.mongodb.database}")
    private String mongodbDatabase;
    
    @Value("${storage.root}")
    private String storageRootFolder;
    
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

    @Value("${storage.collections.upload.tmp}")
    private String collectionsUploadTmpFolder;

    @Value("${storage.temp.jobs}")
    private String jobsTempFolder;

    @Value("${ome.converter.threads:2}")
    private int omeConverterThreads;

    
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
	
	public String getWorkflowManagementSystem() {
		return workflowManagementSystem;
	}

	public String getWorkflowsFolder() {
		return workflowsFolder;
	}
	
    public String getStitchingFolder() {
        return stitchingFolder;
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

    public int getOmeConverterThreads() {
        return omeConverterThreads;
    }

}
