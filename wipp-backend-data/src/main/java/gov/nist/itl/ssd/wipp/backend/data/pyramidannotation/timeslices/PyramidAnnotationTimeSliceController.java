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
package gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.timeslices;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection;
import gov.nist.itl.ssd.wipp.backend.data.pyramid.Pyramid;
import gov.nist.itl.ssd.wipp.backend.data.pyramid.PyramidRepository;
import gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.PyramidAnnotation;
import gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.PyramidAnnotationConfig;
import gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.PyramidAnnotationRepository;
import io.swagger.annotations.Api;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@RestController
@Api(tags="PyramidAnnotation Entity")
@RequestMapping(CoreConfig.BASE_URI + "/pyramidAnnotations/{pyramidAnnotationId}/timeSlices")
@ExposesResourceFor(PyramidAnnotationTimeSlice.class)
public class PyramidAnnotationTimeSliceController {
	
    @Autowired
    private CoreConfig config;
	
	@Autowired
    private PyramidAnnotationRepository pyramidAnnotationRepository;

    @Autowired
    private PyramidAnnotationTimeSliceRepository pyramidAnnotationTimeSliceRepository;

    @Autowired
    private EntityLinks entityLinks;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public HttpEntity<PagedResources<Resource<PyramidAnnotationTimeSlice>>>
            getTimeSlicesPage(
                    @PathVariable("pyramidAnnotationId") String pyramidAnnotationId,
                    @PageableDefault Pageable pageable,
                    PagedResourcesAssembler<PyramidAnnotationTimeSlice> assembler) {

        Page<PyramidAnnotationTimeSlice> page = getPage(
        		pyramidAnnotationRepository.getTimeSlices(pyramidAnnotationId),
                pageable);
        for (PyramidAnnotationTimeSlice pats : page) {
            processResource(pyramidAnnotationId, pats);
        }
        return new ResponseEntity<>(assembler.toResource(page), HttpStatus.OK);
    }

    @RequestMapping(value = "/{timeSliceId}", method = RequestMethod.GET)
    public HttpEntity<PyramidAnnotationTimeSlice> getTimeSlice(
            @PathVariable("pyramidAnnotationId") String pyramidAnnotationId,
            @PathVariable("timeSliceId") int timeSliceId) {
    	PyramidAnnotationTimeSlice pats = pyramidAnnotationTimeSliceRepository
                .findOne(pyramidAnnotationId, timeSliceId);
        if (pats == null) {
            throw new NotFoundException("Time slice " + timeSliceId
                    + " not found in pyramid annotation " + pyramidAnnotationId);
        }
        processResource(pyramidAnnotationId, pats);
        return new ResponseEntity<>(pats, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/{timeSliceId}/annotationPositions",
            method = RequestMethod.GET)
    public void getAnnotationPositions(
            @PathVariable("pyramidAnnotationId") String pyramidAnnotationId,
            @PathVariable("timeSliceId") int timeSliceId,
            HttpServletResponse response) throws IOException {
        File pyramidAnnotationFile = pyramidAnnotationTimeSliceRepository
                .getAnnotationPositionsFile(pyramidAnnotationId, timeSliceId);
        if (!pyramidAnnotationFile.exists()) {
            response.sendError(404);
            return;
        }

        response.setHeader("Content-disposition",
                "attachment;filename=pyramid-annotation-" + pyramidAnnotationId +
                "-" + pyramidAnnotationFile.getName());
        response.setContentType("text/plain");
        try (InputStream is = new BufferedInputStream(
                new FileInputStream(pyramidAnnotationFile))) {
            IOUtils.copy(is, response.getOutputStream());
        }
        response.flushBuffer();
    }
    
    @RequestMapping(
            value = "/{timeSliceId}/annotationPositions",
            method = RequestMethod.POST)
    public PyramidAnnotation uploadAnnotationPositions(
            @PathVariable("pyramidAnnotationId") String pyramidAnnotationId,
            @PathVariable("timeSliceId") int timeSliceId,
            @PathVariable("name") String name,
            @RequestParam("file") MultipartFile file) throws IOException {        
        PyramidAnnotationTimeSlice timeSlice = new PyramidAnnotationTimeSlice(timeSliceId);
		PyramidAnnotation pyramidAnnotation = pyramidAnnotationRepository.findById(pyramidAnnotationId).get();
        List<PyramidAnnotationTimeSlice> allTimeSlices = pyramidAnnotation.getTimeSlices();
        File pyramidAnnotationFolder = new File(config.getPyramidAnnotationsFolder(), pyramidAnnotationId);
        System.out.println("pyramidAnnotationFolder : " + pyramidAnnotationFolder);
		
		if(pyramidAnnotation != null) { 
			System.out.println("Nummber Time Slices : " + pyramidAnnotation.getNumberOfTimeSlices());
			allTimeSlices.add(timeSlice);
			pyramidAnnotation.setTimeSlices(allTimeSlices);
	        pyramidAnnotationRepository.save(pyramidAnnotation);
			System.out.println("Nummber Time Slices : " + pyramidAnnotation.getNumberOfTimeSlices());
			System.out.println("Time Slices : " + pyramidAnnotation.getTimeSlices().toString());
	        file.transferTo(new File(pyramidAnnotationFolder,
	        		PyramidAnnotationConfig.PYRAMID_ANNOTATION_FILENAME_PREFIX + timeSliceId
	                + PyramidAnnotationConfig.PYRAMID_ANNOTATION_FILENAME_SUFFIX));
	        return pyramidAnnotation;
		
		} else {
			
	        List<PyramidAnnotationTimeSlice> timeSlices = Arrays.asList(new PyramidAnnotationTimeSlice(timeSliceId));
			pyramidAnnotation = new PyramidAnnotation(name, timeSlices);
			pyramidAnnotation = pyramidAnnotationRepository.save(pyramidAnnotation);
	        pyramidAnnotationFolder.mkdirs();
	        file.transferTo(new File(pyramidAnnotationFolder,
	        		PyramidAnnotationConfig.PYRAMID_ANNOTATION_FILENAME_PREFIX + timeSliceId
	                + PyramidAnnotationConfig.PYRAMID_ANNOTATION_FILENAME_SUFFIX));
	        return pyramidAnnotation;
		}
    }
    

    private void processResource(String pyramidAnnotationId,
    		PyramidAnnotationTimeSlice pats) {
        // Self
        LinkBuilder lb = entityLinks.linkFor(PyramidAnnotationTimeSlice.class,
        		pyramidAnnotationId);
        Link link = lb.slash(pats.getSliceNumber()).withSelfRel();
        pats.add(link);

        // Annotation positions
        lb = entityLinks.linkFor(PyramidAnnotationTimeSlice.class,
        		pyramidAnnotationId);
        link = lb.slash(pats.getSliceNumber()).slash("annotationPositions")
                .withRel("annotationPositions");
        pats.add(link);
    }

    private Page<PyramidAnnotationTimeSlice> getPage(
            List<PyramidAnnotationTimeSlice> timeSlices, Pageable pageable) {
        if (timeSlices == null) {
            return new PageImpl<>(new ArrayList<>(0), pageable, 0);
        }
        long offset = pageable.getOffset();
        if (offset >= timeSlices.size()) {
            return new PageImpl<>(
                    new ArrayList<>(0), pageable, timeSlices.size());
        }

        Stream<PyramidAnnotationTimeSlice> stream = timeSlices.stream();
        Sort sort = pageable.getSort();
        Comparator<PyramidAnnotationTimeSlice> comparator
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
        List<PyramidAnnotationTimeSlice> result = stream
                .sorted(comparator)
                .skip(offset)
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
        return new PageImpl<>(result, pageable, timeSlices.size());
    }

}
