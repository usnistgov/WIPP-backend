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
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.data.stitching.timeslices.StitchingVectorTimeSlice;


/**
*
* @author Antoine Vandecreme
*/
public class StitchingVectorRepositoryImpl
       implements StitchingVectorRepositoryCustom {

   @Autowired
   private CoreConfig config;

   @Autowired
   private MongoTemplate mongoTemplate;

   @Override
   public void setTimeSlices(String stitchingVectorId,
           List<StitchingVectorTimeSlice> timeSlices) {
       mongoTemplate.updateFirst(
               Query.query(Criteria.where("id").is(stitchingVectorId)),
               new Update().set("timeSlices", timeSlices),
               StitchingVector.class);
   }

   @Override
   public List<StitchingVectorTimeSlice> getTimeSlices(
           String stitchingVectorId) {
       Query query = Query.query(Criteria.where("id").is(stitchingVectorId));
       query.fields().include("timeSlices");
       StitchingVector stitchingVector = mongoTemplate.findOne(
               query, StitchingVector.class);
       return stitchingVector.getTimeSlices();
   }

   @Override
   public File getStatisticsFile(String stitchingVectorId) {
       return new File(
               new File(config.getStitchingFolder(), stitchingVectorId),
               CoreConfig.STITCHING_VECTOR_STATISTICS_FILE_NAME);
   }

}
