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
package gov.nist.itl.ssd.wipp.backend.data.pyramidannotation;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.BaseDataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobExecutionException;
import gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.timeslices.PyramidAnnotationTimeSlice;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@Component("pyramidAnnotationDataHandler")
public class PyramidAnnotationDataHandler extends BaseDataHandler implements DataHandler {

    @Autowired
    CoreConfig config;
    
    @Autowired
    private PyramidAnnotationRepository pyramidAnnotationRepository;

    public PyramidAnnotationDataHandler() {
    }
		
    @Override
    public void importData(Job job, String outputName) throws IOException, JobExecutionException {

        String pyramidAnnotationFilenamePattern =
        		PyramidAnnotationConfig.PYRAMID_ANNOTATION_FILENAME_PREFIX
                        + "([0-9]+)"
                        + PyramidAnnotationConfig.PYRAMID_ANNOTATION_FILENAME_SUFFIX;
        File jobTempOutputFolder = getJobOutputTempFolder(job.getId(), outputName);

        List<PyramidAnnotationTimeSlice> timeSlices = Stream
                .of(jobTempOutputFolder.listFiles((d, name) -> name.matches(pyramidAnnotationFilenamePattern)))
                .map(f -> createTimeSlice(f.getName()))
                .collect(Collectors.toList());
        
        PyramidAnnotation pyramidAnnotation = new PyramidAnnotation(job, timeSlices, outputName);
        // We save so that an Id is generated.
        pyramidAnnotationRepository.save(pyramidAnnotation);

        new File(config.getPyramidAnnotationsFolder()).mkdirs();
        File pyramidAnnotationFolder = new File(
                config.getPyramidAnnotationsFolder(), pyramidAnnotation.getId());
        boolean success = getJobOutputTempFolder(job.getId(), outputName).renameTo(pyramidAnnotationFolder);
        if (!success) {
        	pyramidAnnotationRepository.delete(pyramidAnnotation);
            throw new JobExecutionException(
                    "Cannot move pyramid annotation to final destination.");
        }
        
        setOutputId(job, outputName, pyramidAnnotation.getId());
    }
	

	@Override
	public String exportDataAsParam(String value) {
        String pyramidAnnotationId = value;
        String pyramidAnnotationPath;

        // check if the input of the job is the output of another job and if so return the associated path
        String regex = "\\{\\{ (.*)\\.(.*) \\}\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(pyramidAnnotationId);
        if (m.find()) {
            String jobId = m.group(1);
            String outputName = m.group(2);
            pyramidAnnotationPath = getJobOutputTempFolder(jobId, outputName).getAbsolutePath();
        }
        // else return the path of the notebook
        else {
            File pyramidAnnotationFolder = new File(config.getPyramidAnnotationsFolder(), pyramidAnnotationId);
            pyramidAnnotationPath = pyramidAnnotationFolder.getAbsolutePath();

        }
        pyramidAnnotationPath = pyramidAnnotationPath.replaceFirst(config.getStorageRootFolder(),config.getContainerInputsMountPath());
        return pyramidAnnotationPath;
	}
	
    private PyramidAnnotationTimeSlice createTimeSlice(String filename) {
        int timeSlice = Integer.valueOf(
                StringUtils.substringBetween(filename,
                        PyramidAnnotationConfig.PYRAMID_ANNOTATION_FILENAME_PREFIX,
                        PyramidAnnotationConfig.PYRAMID_ANNOTATION_FILENAME_SUFFIX));
        return new PyramidAnnotationTimeSlice(timeSlice);
    }
	
}
