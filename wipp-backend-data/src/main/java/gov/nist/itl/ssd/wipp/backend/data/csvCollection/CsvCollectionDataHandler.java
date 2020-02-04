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
package gov.nist.itl.ssd.wipp.backend.data.csvCollection;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nist.itl.ssd.wipp.backend.data.csvCollection.csv.CsvHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.BaseDataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobExecutionException;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@Component("csvCollectionDataHandler")
public class CsvCollectionDataHandler  extends BaseDataHandler implements DataHandler{


    @Autowired
    CoreConfig config;

    @Autowired
    private CsvCollectionRepository csvCollectionRepository;

    @Autowired
    private CsvHandler csvHandler;

    @Override
    public void importData(Job job, String outputName) throws JobExecutionException, IOException {
        CsvCollection csvCollection = new CsvCollection(job, outputName);
        csvCollectionRepository.save(csvCollection);


        File csvCollectionFolder = new File(config.getCsvCollectionsFolder(), csvCollection.getId());
        csvCollectionFolder.mkdirs();

        File tempOutputDir = getJobOutputTempFolder(job.getId(), outputName);
        importFolder(csvCollectionFolder, csvCollection.getId());
        boolean success = tempOutputDir.renameTo(csvCollectionFolder);
        if (!success) {
            csvCollectionRepository.delete(csvCollection);
            throw new JobExecutionException("Cannot move csv collection to final destination.");
        }
        setOutputId(job, outputName, csvCollection.getId());
    }

    public String exportDataAsParam(String value) {
        String csvCollectionId = value;
        String csvCollectionPath;

        // check if the input of the job is the output of another job and if so return the associated path
        String regex = "\\{\\{ (.*)\\.(.*) \\}\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(csvCollectionId);
        if (m.find()) {
            String jobId = m.group(1);
            String outputName = m.group(2);
            csvCollectionPath = getJobOutputTempFolder(jobId, outputName).getAbsolutePath();
        }
        // else return the path of the csv collection
        else {
            File csvCollectionFolder = new File(config.getCsvCollectionsFolder(), csvCollectionId);
            csvCollectionPath = csvCollectionFolder.getAbsolutePath();

        }
        csvCollectionPath = csvCollectionPath.replaceFirst(config.getStorageRootFolder(),config.getContainerInputsMountPath());
        return csvCollectionPath;
    }

    private void importFolder(File file, String id) throws IOException {
        csvHandler.importFolder(id, file);
    }

}
