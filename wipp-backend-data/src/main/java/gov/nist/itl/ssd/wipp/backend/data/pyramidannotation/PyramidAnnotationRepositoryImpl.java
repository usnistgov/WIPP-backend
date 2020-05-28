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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.timeslices.PyramidAnnotationTimeSlice;
import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVector;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
public class PyramidAnnotationRepositoryImpl implements PyramidAnnotationRepositoryCustom {
	
	   @Autowired
	   private MongoTemplate mongoTemplate;

	   @Override
	   public void setTimeSlices(String pyramidAnnotationId,
	           List<PyramidAnnotationTimeSlice> timeSlices) {
	       mongoTemplate.updateFirst(
	               Query.query(Criteria.where("id").is(pyramidAnnotationId)),
	               new Update().set("timeSlices", timeSlices),
	               StitchingVector.class);
	   }

	   @Override
	   public List<PyramidAnnotationTimeSlice> getTimeSlices(
	           String pyramidAnnotationId) {
	       Query query = Query.query(Criteria.where("id").is(pyramidAnnotationId));
	       query.fields().include("timeSlices");
	       PyramidAnnotation pyramidAnnotation = mongoTemplate.findOne(
	               query, PyramidAnnotation.class);
	       return pyramidAnnotation.getTimeSlices();
	   }
}
