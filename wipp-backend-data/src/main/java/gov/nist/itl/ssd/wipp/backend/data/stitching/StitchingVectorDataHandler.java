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
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gov.nist.itl.ssd.wipp.backend.core.model.data.BaseDataHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobExecutionException;
import gov.nist.itl.ssd.wipp.backend.data.stitching.timeslices.StitchingVectorTimeSlice;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Component("stitchingVectorDataHandler")
public class StitchingVectorDataHandler extends BaseDataHandler implements DataHandler{

    @Autowired
    CoreConfig config;

    @Autowired
    private StitchingVectorRepository stitchingVectorRepository;

    private static final Logger LOG = Logger.getLogger(StitchingVectorDataHandler.class.getName());

    public StitchingVectorDataHandler() {
    }

    @Override
    public void importData(Job job, String outputName) throws IOException, JobExecutionException {

        String stitchingVectorFilenamePattern =
                StitchingVectorConfig.STITCHING_VECTOR_GLOBAL_POSITION_PREFIX
                        + "([0-9]+)"
                        + StitchingVectorConfig.STITCHING_VECTOR_FILENAME_SUFFIX;
        File jobTempOutputFolder = getJobOutputTempFolder(job.getId(), outputName);

        List<StitchingVectorTimeSlice> timeSlices = Stream
                .of(jobTempOutputFolder.listFiles((d, name) -> name.matches(stitchingVectorFilenamePattern)))
                .map(f -> createTimeSlice(f.getName()))
                .collect(Collectors.toList());

        StitchingVector vector = new StitchingVector(job, timeSlices);
        // We save so that an Id is generated.
        stitchingVectorRepository.save(vector);

        new File(config.getStitchingFolder()).mkdirs();
        File stitchingVectorFolder = new File(
                config.getStitchingFolder(), vector.getId());
        boolean success = getJobOutputTempFolder(job.getId(), outputName).renameTo(stitchingVectorFolder);
        if (!success) {
            stitchingVectorRepository.delete(vector);
            throw new JobExecutionException(
                    "Cannot move stitching vector to final destination.");
        }

        setOutputId(job, outputName, vector.getId());

    }

    public String exportDataAsParam(String value) {
        String stitchingVectorId = value;
        String stitchingVectorPath;

        // check if the input of the job is the output of another job and if so return the associated path
        String regex = "\\{\\{ (.*)\\.(.*) \\}\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(stitchingVectorId);
        if (m.find()) {
            String jobId = m.group(1);
            String outputName = m.group(2);
            stitchingVectorPath = getJobOutputTempFolder(jobId, outputName).getAbsolutePath();
        }
        // else return the path of the stitching vector
        else {
            File stitchingVectorFolder = new File(config.getStitchingFolder(), stitchingVectorId);
            stitchingVectorPath = stitchingVectorFolder.getAbsolutePath();

        }
        stitchingVectorPath = stitchingVectorPath.replaceFirst(config.getStorageRootFolder(),config.getContainerInputsMountPath());
        return stitchingVectorPath;

    }

    private StitchingVectorTimeSlice createTimeSlice(String filename) {
        String errorMessage = "PASSED";
        int timeSlice = Integer.valueOf(
                StringUtils.substringBetween(filename,
                        StitchingVectorConfig.STITCHING_VECTOR_GLOBAL_POSITION_PREFIX,
                        StitchingVectorConfig.STITCHING_VECTOR_FILENAME_SUFFIX));
        return new StitchingVectorTimeSlice(timeSlice, errorMessage);
    }

}
