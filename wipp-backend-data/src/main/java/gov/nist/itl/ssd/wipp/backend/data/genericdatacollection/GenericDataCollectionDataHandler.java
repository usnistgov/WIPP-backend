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
package gov.nist.itl.ssd.wipp.backend.data.genericdatacollection;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.BaseDataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobExecutionException;
import gov.nist.itl.ssd.wipp.backend.data.genericdatacollection.genericfiles.GenericFileHandler;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
* @author Mylene Simon <mylene.simon at nist.gov>
*/
@Component("genericDataDataHandler")
public class GenericDataCollectionDataHandler extends BaseDataHandler implements DataHandler{

	@Autowired
	CoreConfig config;

	@Autowired
	private GenericDataCollectionRepository genericDataCollectionRepository;
	
    @Autowired
    private GenericFileHandler genericFileHandler;

	@Override
	public void importData(Job job, String outputName) throws JobExecutionException {
		GenericDataCollection genericDataCollection = new GenericDataCollection(job, outputName);
		// Set genericDataCollection owner to job owner
		genericDataCollection.setOwner(job.getOwner());
		// Set genericDataCollection to private
		genericDataCollection.setPubliclyShared(false);
		genericDataCollectionRepository.save(genericDataCollection);

		File genericDataCollectionFolder = new File(config.getGenericDataCollectionsFolder(), genericDataCollection.getId());
		genericDataCollectionFolder.mkdirs();

		try {
			File tempOutputDir = getJobOutputTempFolder(job.getId(), outputName);
			genericFileHandler.importFolder(genericDataCollection.getId(), tempOutputDir);
			setOutputId(job, outputName, genericDataCollection.getId());
		} catch (IOException ex) {
			genericDataCollectionRepository.delete(genericDataCollection);
			throw new JobExecutionException("Cannot move generic data collection to final destination.");
		}
		
		// search for metadata file
		File[] metadataFiles = genericDataCollectionFolder.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.equals("data-info.json");
		    }
		});
		
		// parse metadata file
		if (metadataFiles != null && metadataFiles.length != 0){
			File metadataFile = metadataFiles[0];
			JSONParser parser = new JSONParser();
			try {
				Object obj = parser.parse(new FileReader(metadataFile.getAbsolutePath()));
				
		        // typecasting obj to JSONObject 
		        JSONObject jo = (JSONObject) obj; 
		          
		        // get type, description and metadata 
		        String type = (String) jo.get("type"); 
		        String description = (String) jo.get("description"); 
		        String metadata = (String) jo.get("metadata");
				
				genericDataCollection.setType(type);
				genericDataCollection.setDescription(description);
				genericDataCollection.setMetadata(metadata);
				
				// updating generic data collection
				genericDataCollectionRepository.save(genericDataCollection);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
    public String exportDataAsParam(String value) {
        String genericDataCollectionId = value;
        String genericDataCollectionPath;

        // check if the input of the job is the output of another job and if so return the associated path
        String regex = "\\{\\{ (.*)\\.(.*) \\}\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(genericDataCollectionId);
        if (m.find()) {
            String jobId = m.group(1);
            String outputName = m.group(2);
            genericDataCollectionPath = getJobOutputTempFolder(jobId, outputName).getAbsolutePath();
        }
        // else return the path of the generic data collection
        else {
            File genericDataCollectionFolder = new File(config.getGenericDataCollectionsFolder(), genericDataCollectionId);
            genericDataCollectionPath = genericDataCollectionFolder.getAbsolutePath();

        }
        genericDataCollectionPath = genericDataCollectionPath.replaceFirst(config.getStorageRootFolder(),config.getContainerInputsMountPath());
        return genericDataCollectionPath;

    }
    
    @Override
    public void setDataToPublic(String value) {
    	Optional<GenericDataCollection> optGenericDataCollection = genericDataCollectionRepository.findById(value);
        if(optGenericDataCollection.isPresent()) {
        	GenericDataCollection genericDataCollection = optGenericDataCollection.get();
            if (!genericDataCollection.isPubliclyShared()) {
            	genericDataCollection.setPubliclyShared(true);
            	genericDataCollectionRepository.save(genericDataCollection);
            }
        }
    }
	
}
