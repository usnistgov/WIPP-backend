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
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.timeslices.PyramidAnnotationTimeSlice;
import io.swagger.annotations.Api;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@RestController
@Api(tags="PyramidAnnotation Entity")
@RequestMapping(CoreConfig.BASE_URI + "/pyramidAnnotations/upload")
public class PyramidAnnotationUploadController {

    @Autowired
    private CoreConfig config;

    @Autowired
    private PyramidAnnotationRepository pyramidAnnotationRepository;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public PyramidAnnotation upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name)
            throws IOException {
        if (name == null || name.isEmpty()) {
            throw new ClientException(
                    "A pyramid annotation name must be specified.");
        }

        List<PyramidAnnotationTimeSlice> timeSlices = Arrays.asList(
                new PyramidAnnotationTimeSlice(1));
        PyramidAnnotation pyramidAnnotation = new PyramidAnnotation(name, timeSlices);
        
        pyramidAnnotation = pyramidAnnotationRepository.save(pyramidAnnotation);
        File pyramidAnnotationFolder = new File(
                config.getPyramidAnnotationsFolder(),
                pyramidAnnotation.getId());
        pyramidAnnotationFolder.mkdirs();
        file.transferTo(new File(pyramidAnnotationFolder,
        		PyramidAnnotationConfig.PYRAMID_ANNOTATION_FILENAME_PREFIX + "1"
                + PyramidAnnotationConfig.PYRAMID_ANNOTATION_FILENAME_SUFFIX));
        return pyramidAnnotation;
    }
}
