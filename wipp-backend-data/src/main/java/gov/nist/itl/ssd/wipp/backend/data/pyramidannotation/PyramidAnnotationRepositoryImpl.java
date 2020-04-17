package gov.nist.itl.ssd.wipp.backend.data.pyramidannotation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.data.pyramidannotation.timeslices.PyramidAnnotationTimeSlice;
import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVector;

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
