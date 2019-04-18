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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@Component("stitchingVectorDataHandler")
public class StitchingVectorDataHandler implements DataHandler{

    @Autowired
    CoreConfig config;

    @Autowired
    private StitchingVectorRepository stitchingVectorRepository;

    public StitchingVectorDataHandler() {
    }

    @Override
    public void importData(Job job, String outputName) throws IOException {
		StitchingVector outputStitchingVector= new StitchingVector(job, outputName);
		outputStitchingVector = stitchingVectorRepository.save(outputStitchingVector);

		try {
        	 File stitchingVectorFolder = new File(config.getStitchingFolder(), outputStitchingVector.getId());
        	 stitchingVectorFolder.mkdirs();
        	 Files.move(getJobOutputTempFolder(job, outputName).toPath(), stitchingVectorFolder.toPath());
        } catch (IOException ex) {
        	stitchingVectorRepository.delete(outputStitchingVector);
            throw ex;
        }
    }

    public String exportDataAsParam(String value) {
        String stitchingVectorId = value;
        File inputStitchingVectorFolder = new File(config.getStitchingFolder(), stitchingVectorId);
        String stitchingVectorPath = inputStitchingVectorFolder.getAbsolutePath();
        stitchingVectorPath = changeContainerPath(stitchingVectorPath);
        return stitchingVectorPath;
    }

    private final File getJobOutputTempFolder(Job job, String outputName) {
        return new File( new File(config.getJobsTempFolder(), job.getId()), outputName);
    }

      private final String changeContainerPath(String formerPath){
        String newPathPrefix = "/data/inputs";
        int cuttingIndex = formerPath.indexOf("/WIPP-plugins") + "/WIPP-plugins".length();
        String newPath = newPathPrefix + formerPath.substring(cuttingIndex);
        return newPath;
    }
}
