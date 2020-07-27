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
package gov.nist.itl.ssd.wipp.backend.data.pyramid.timeslices;

//import gov.nist.itl.ssd.wipp.wippcore.CoreConfig;
//import gov.nist.itl.ssd.wipp.wippcore.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
//import io.swagger.annotations.Api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Antoine Vandecreme
 */
@RestController
//@Api(tags="Pyramid Entity")
@RequestMapping(CoreConfig.BASE_URI + "/pyramids/{pyramidId}/timeSlices")
@ExposesResourceFor(PyramidTimeSlice.class)
public class PyramidTimeSliceController {

	 @Autowired
	    private PyramidTimeSliceRepository pyramidTimeSliceRepository;

	    @Autowired
	    private EntityLinks entityLinks;

	    @RequestMapping(value = "", method = RequestMethod.GET)
	    public HttpEntity<PagedResources<Resource<PyramidTimeSlice>>>
	            getTimeSlicesPage(
	                    @PathVariable("pyramidId") String pyramidId,
	                    @PageableDefault Pageable pageable,
	                    PagedResourcesAssembler<PyramidTimeSlice> assembler) {

	        Page<PyramidTimeSlice> page = getPage(
	                pyramidTimeSliceRepository.findAll(pyramidId), pageable);
	        for (PyramidTimeSlice pts : page) {
	            processResource(pyramidId, pts);
	        }
	        return new ResponseEntity<>(assembler.toResource(page), HttpStatus.OK);
	    }

	    @RequestMapping(value = "/{timeSliceId}", method = RequestMethod.GET)
	    public HttpEntity<PyramidTimeSlice> getTimeSlice(
	            @PathVariable("pyramidId") String pyramidId,
	            @PathVariable("timeSliceId") String timeSliceId) {
	        PyramidTimeSlice pts = pyramidTimeSliceRepository.findOne(
	                pyramidId, timeSliceId);
	        if (pts == null) {
	            throw new NotFoundException("Time slice " + timeSliceId
	                    + " not found in pyramid " + pyramidId);
	        }
	        processResource(pyramidId, pts);
	        return new ResponseEntity<>(pts, HttpStatus.OK);
	    }

	    private void processResource(String pyramidId, PyramidTimeSlice pts) {
	        // Self
	        LinkBuilder lb = entityLinks.linkFor(PyramidTimeSlice.class, pyramidId);
	        Link link = lb.slash(pts.getName()).withSelfRel();
	        pts.add(link);

	        String selfUri = link.getHref();
	        String timeSliceBaseUri = CoreConfig.BASE_URI + "/pyramids/"
	                + pyramidId + "/timeSlices/" + pts.getName();
	        String timeSliceFullUri = selfUri.replace(timeSliceBaseUri,
	        		CoreConfig.PYRAMIDS_BASE_URI + "/" + pyramidId + "/"
	                        + pts.getName());

	        // DZI
	        String dziUri = timeSliceFullUri + ".dzi";
	        pts.add(new Link(dziUri, "dzi"));

	        // OME
	        String omeUri = timeSliceFullUri + ".ome.xml";
	        pts.add(new Link(omeUri, "ome"));
	    }

	    private Page<PyramidTimeSlice> getPage(List<PyramidTimeSlice> timeSlices,
	            Pageable pageable) {
	        if (timeSlices == null) {
	            return new PageImpl<>(new ArrayList<>(0), pageable, 0);
	        }
	        long offset = pageable.getOffset();
	        if (offset >= timeSlices.size()) {
	            return new PageImpl<>(
	                    new ArrayList<>(0), pageable, timeSlices.size());
	        }

	        Stream<PyramidTimeSlice> stream = timeSlices.stream();
	        Sort sort = pageable.getSort();
	        Comparator<PyramidTimeSlice> comparator
	                = (pts1, pts2) -> pts1.getName().compareTo(pts2.getName());
	        if (sort != null) {
	            for (Sort.Order order : sort) {
	                if ("name".equals(order.getProperty())
	                        && order.getDirection() == Sort.Direction.DESC) {
	                    comparator = comparator.reversed();
	                }
	            }
	        }
	        List<PyramidTimeSlice> result = stream.sorted(comparator).skip(offset)
	                .limit(pageable.getPageSize()).collect(Collectors.toList());
	        return new PageImpl<>(result, pageable, timeSlices.size());
	    }

}
