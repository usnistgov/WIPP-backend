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
    public static final String PYRAMIDS_BASE_URI = "/pyramid-files";
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
    private String workflowBinary;

    @Value("${workflow.nodeSelector}")
    private String workflowNodeSelector;

    @Value("${workflow.tolerations}")
    private String workflowTolerations;
    
    @Value("${kube.wippdata.pvc}")
    private String wippDataPVCName;

    @Value("${storage.collections}")
    private String imagesCollectionsFolder;

    @Value("${storage.stitching}")
    private String stitchingFolder;
    
    @Value("${storage.pyramids}")
    private String pyramidsFolder;
    
    @Value("${storage.pyramid.annotations}")
    private String pyramidAnnotationsFolder;
    
    @Value("${storage.tensorflow}")
    private String tensorflowModelsFolder;
    
    @Value("${storage.tensorboard}")
    private String tensorboardLogsFolder;
    
    @Value("${storage.csv.collections}")
    private String csvCollectionsFolder;
    
    @Value("${storage.notebooks}")
    private String notebooksFolder;
    
    @Value("${storage.generic.datas}")
    private String genericDatasFolder;
    
    @Value("${storage.generic.datas.upload.tmp}")
    private String genericDatasUploadTmpFolder;

    @Value("${storage.notebooks.tmp}")
    private String notebooksTmpFolder;
    
    @Value("${storage.collections.upload.tmp}")
    private String collectionsUploadTmpFolder;

    @Value("${storage.csvCollections.upload.tmp}")
    private String csvCollectionsUploadTmpFolder;

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
    
    public String getPyramidAnnotationsFolder() {
		return pyramidAnnotationsFolder;
	}

	public String getTensorflowModelsFolder() {
    	return tensorflowModelsFolder;
    }
    
   	public String getTensorboardLogsFolder() {
		return tensorboardLogsFolder;
	}
   	
	public String getCsvCollectionsFolder() {
		return csvCollectionsFolder;
	}

	public String getNotebooksFolder() {
		return notebooksFolder;
	}

	public String getNotebooksTmpFolder() {
		return notebooksTmpFolder;
	}
	
    public String getGenericDatasFolder() {
		return genericDatasFolder;
	}
    
    public String getGenericDatasUploadTmpFolder() {
		return genericDatasUploadTmpFolder;
	}

    public String getWorkflowBinary() {
        return workflowBinary;
    }

    public String getWorkflowNodeSelector() {
        return workflowNodeSelector;
    }

    public String getWorkflowTolerations() {
        return workflowTolerations;
    }

	public String getWippDataPVCName() {
		return wippDataPVCName;
	}

	public String getImagesCollectionsFolder() {
        return imagesCollectionsFolder;
    }

	public String getCollectionsUploadTmpFolder() {
        return collectionsUploadTmpFolder;
    }

    public String getCsvCollectionsUploadTmpFolder() {
        return csvCollectionsUploadTmpFolder;
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
