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
package gov.nist.itl.ssd.wipp.backend.data.stitching.timeslices;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

import gov.nist.itl.ssd.wipp.backend.core.utils.SecurityUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVectorRepository;
import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataDownloadToken;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataDownloadTokenRepository;
import gov.nist.itl.ssd.wipp.backend.core.rest.DownloadUrl;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;


/**
 *
 * @author Antoine Vandecreme
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@RestController
@Tag(name="StitchingVector Entity")
@RequestMapping(CoreConfig.BASE_URI + "/stitchingVectors/{stitchingVectorId}/timeSlices")
@ExposesResourceFor(StitchingVectorTimeSlice.class)
public class StitchingVectorTimeSliceController {

    @Autowired
    private StitchingVectorRepository stitchingVectorRepository;

    @Autowired
    private StitchingVectorTimeSliceRepository stitchingVectorTimeSliceRepository;
    
    @Autowired
    private DataDownloadTokenRepository dataDownloadTokenRepository;

    @Autowired
    private EntityLinks entityLinks;

    @RequestMapping(value = "", method = RequestMethod.GET)
	@PreAuthorize("hasRole('admin') or @stitchingVectorSecurity.checkAuthorize(#stitchingVectorId, false)")
    public HttpEntity<PagedModel<EntityModel<StitchingVectorTimeSlice>>>
            getTimeSlicesPage(
                    @PathVariable("stitchingVectorId") String stitchingVectorId,
                    @PageableDefault Pageable pageable,
                    PagedResourcesAssembler<StitchingVectorTimeSlice> assembler) {

        Page<StitchingVectorTimeSlice> page = getPage(
                stitchingVectorRepository.getTimeSlices(stitchingVectorId),
                pageable);
        for (StitchingVectorTimeSlice svts : page) {
            processResource(stitchingVectorId, svts);
        }
        return new ResponseEntity<>(assembler.toModel(page), HttpStatus.OK);
    }

    @RequestMapping(value = "/{timeSliceId}", method = RequestMethod.GET)
	@PreAuthorize("hasRole('admin') or @stitchingVectorSecurity.checkAuthorize(#stitchingVectorId, false)")
    public HttpEntity<StitchingVectorTimeSlice> getTimeSlice(
            @PathVariable("stitchingVectorId") String stitchingVectorId,
            @PathVariable("timeSliceId") int timeSliceId) {
        StitchingVectorTimeSlice svts = stitchingVectorTimeSliceRepository
                .findOne(stitchingVectorId, timeSliceId);
        if (svts == null) {
            throw new NotFoundException("Time slice " + timeSliceId
                    + " not found in stitching vector " + stitchingVectorId);
        }
        processResource(stitchingVectorId, svts);
        return new ResponseEntity<>(svts, HttpStatus.OK);
    }
    
    @RequestMapping(
            value = "/{timeSliceId}/globalPositions/request",
            method = RequestMethod.GET,
            produces = "application/json")
	@PreAuthorize("hasRole('admin') or @stitchingVectorSecurity.checkAuthorize(#stitchingVectorId, false)")
    public DownloadUrl requestDownload(
            @PathVariable("stitchingVectorId") String stitchingVectorId,
            @PathVariable("timeSliceId") int timeSliceId) {
    	
    	// Check existence of images collection
    	StitchingVectorTimeSlice ts = stitchingVectorTimeSliceRepository.findOne(stitchingVectorId,
    			timeSliceId);
        if (ts == null) {
            throw new ResourceNotFoundException(
                    "Images collection " + stitchingVectorId + " not found.");
        }
        
        // Generate download token
        DataDownloadToken downloadToken = new DataDownloadToken(stitchingVectorId);
        dataDownloadTokenRepository.save(downloadToken);
        
        // Generate and send unique download URL
        String tokenParam = "?token=" + downloadToken.getToken();
        String timeSliceGlobalPositions = "/" + timeSliceId + "/globalPositions";
        String downloadLink = linkTo(StitchingVectorTimeSliceController.class,
        		stitchingVectorId).toString() + timeSliceGlobalPositions + tokenParam;
        return new DownloadUrl(downloadLink);
    }

    @RequestMapping(
            value = "/{timeSliceId}/globalPositions",
            method = RequestMethod.GET)
    public void getGlobalPositions(
            @PathVariable("stitchingVectorId") String stitchingVectorId,
            @PathVariable("timeSliceId") int timeSliceId,
            @RequestParam("token")String token,
            HttpServletResponse response) throws IOException {
    	
    	// Check validity of download token
    	Optional<DataDownloadToken> downloadToken = dataDownloadTokenRepository.findByToken(token);
    	if (!downloadToken.isPresent() || !downloadToken.get().getDataId().equals(stitchingVectorId)) {
    		throw new ForbiddenException("Invalid download token.");
    	}

        // Load security context for system operations
        SecurityUtils.runAsSystem();
    	
    	// Check existence of stitching file
        File stitchingFile = stitchingVectorTimeSliceRepository
                .getGlobalPositionsFile(stitchingVectorId, timeSliceId);

        // Clear security context after system operations
        SecurityContextHolder.clearContext();

        if (!stitchingFile.exists()) {
            response.sendError(404);
            return;
        }

        response.setHeader("Content-disposition",
                "attachment;filename=stitching-vector-" + stitchingVectorId +
                "-" + stitchingFile.getName());
        response.setContentType("text/plain");
        try (InputStream is = new BufferedInputStream(
                new FileInputStream(stitchingFile))) {
            IOUtils.copy(is, response.getOutputStream());
        }
        response.flushBuffer();
    }

    private void processResource(String stitchingVectorId,
            StitchingVectorTimeSlice svts) {
        // Self
        LinkBuilder lb = entityLinks.linkFor(StitchingVectorTimeSlice.class,
                stitchingVectorId);
        Link link = lb.slash(svts.getSliceNumber()).withSelfRel();
        //svts.add(link);

        // Global positions
        lb = entityLinks.linkFor(StitchingVectorTimeSlice.class,
                stitchingVectorId);
        link = lb.slash(svts.getSliceNumber()).slash("globalPositions").slash("request")
                .withRel("globalPositions");
        //svts.add(link);
    }

    private Page<StitchingVectorTimeSlice> getPage(
            List<StitchingVectorTimeSlice> timeSlices, Pageable pageable) {
        if (timeSlices == null) {
            return new PageImpl<>(new ArrayList<>(0), pageable, 0);
        }
        long offset = pageable.getOffset();
        if (offset >= timeSlices.size()) {
            return new PageImpl<>(
                    new ArrayList<>(0), pageable, timeSlices.size());
        }

        Stream<StitchingVectorTimeSlice> stream = timeSlices.stream();
        Sort sort = pageable.getSort();
        Comparator<StitchingVectorTimeSlice> comparator
                = (pts1, pts2) -> Integer.compare(
                        pts1.getSliceNumber(), pts2.getSliceNumber());
        if (sort != null) {
            for (Sort.Order order : sort) {
                if ("sliceNumber".equals(order.getProperty())
                        && order.getDirection() == Sort.Direction.DESC) {
                    comparator = comparator.reversed();
                }
            }
        }
        List<StitchingVectorTimeSlice> result = stream
                .sorted(comparator)
                .skip(offset)
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
        return new PageImpl<>(result, pageable, timeSlices.size());
    }

}
