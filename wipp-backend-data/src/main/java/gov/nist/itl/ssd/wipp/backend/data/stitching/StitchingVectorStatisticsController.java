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
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataDownloadToken;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataDownloadTokenRepository;
import gov.nist.itl.ssd.wipp.backend.core.rest.DownloadUrl;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Antoine Vandecreme
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@RestController
@Tag(name="StitchingVector Entity")
@RequestMapping(CoreConfig.BASE_URI + "/stitchingVectors/{stitchingVectorId}/statistics")
public class StitchingVectorStatisticsController {

    @Autowired
    private StitchingVectorRepository stitchingVectorRepository;
    
    @Autowired
	DataDownloadTokenRepository dataDownloadTokenRepository;

    @RequestMapping(
            value = "request",
            method = RequestMethod.GET,
            produces = "application/json")
	@PreAuthorize("hasRole('admin') or @stitchingVectorSecurity.checkAuthorize(#stitchingVectorId, false)")
	public DownloadUrl requestDownload(
            @PathVariable("stitchingVectorId") String stitchingVectorId) {
    	
    	// Check existence of Stitching vector
    	Optional<StitchingVector> sv = stitchingVectorRepository.findById(
    			stitchingVectorId);
        if (!sv.isPresent()) {
            throw new ResourceNotFoundException(
                    "Sitching vector " + stitchingVectorId + " not found.");
        }
        
        // Generate download token
        DataDownloadToken downloadToken = new DataDownloadToken(stitchingVectorId);
        dataDownloadTokenRepository.save(downloadToken);
        
        // Generate and send unique download URL
        String tokenParam = "?token=" + downloadToken.getToken();
        String downloadLink = linkTo(StitchingVectorStatisticsController.class,
        		stitchingVectorId).toString() + tokenParam;
        return new DownloadUrl(downloadLink);
    }
    
    @RequestMapping(value = "", method = RequestMethod.GET)
    public void getStatistics(
            @PathVariable("stitchingVectorId") String stitchingVectorId,
            @RequestParam("token")String token,
            HttpServletResponse response) throws IOException {

    	// Check validity of download token
    	Optional<DataDownloadToken> downloadToken = dataDownloadTokenRepository.findByToken(token);
    	if (!downloadToken.isPresent() || !downloadToken.get().getDataId().equals(stitchingVectorId)) {
    		throw new ForbiddenException("Invalid download token.");
    	}
    	
    	// Check existence of statistics file
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
