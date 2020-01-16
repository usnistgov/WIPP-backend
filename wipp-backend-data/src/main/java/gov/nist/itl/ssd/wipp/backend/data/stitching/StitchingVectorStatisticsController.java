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

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
//import io.swagger.annotations.Api;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Antoine Vandecreme
 */
@RestController
//@Api(tags="StitchingVector Entity")
@RequestMapping(CoreConfig.BASE_URI + "/stitchingVectors/{stitchingVectorId}/statistics")
public class StitchingVectorStatisticsController {

    @Autowired
    private StitchingVectorRepository stitchingVectorRepository;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public void getStatistics(
            @PathVariable("stitchingVectorId") String stitchingVectorId,
            HttpServletResponse response) throws IOException {

        File statisticsFile = stitchingVectorRepository.getStatisticsFile(
                stitchingVectorId);
        if (!statisticsFile.exists()) {
            response.sendError(404);
            return;
        }
        response.setHeader("Content-disposition",
                "attachment;filename=stitching-vector-" + stitchingVectorId
                + "-" + statisticsFile.getName());
        response.setContentType("text/plain");
        try (InputStream is = new BufferedInputStream(
                new FileInputStream(statisticsFile))) {
            IOUtils.copy(is, response.getOutputStream());
        }
        response.flushBuffer();
    }

}
