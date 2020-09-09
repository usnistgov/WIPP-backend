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
package gov.nist.itl.ssd.wipp.backend.data.genericdata.genericfiles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi@nist.gov>
*/
public class GenericFileRepositoryImpl implements GenericFileRepositoryCustom{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void deleteByGenericData(String genericData) {
        mongoTemplate.remove(Query.query(
                Criteria.where("genericData").is(genericData)),
        		GenericFile.class);
    }

    @Override
    public void deleteByGenericDataAndFileName(String genericData,
            String fileName) {
        mongoTemplate.remove(Query.query(
                Criteria.where("genericData").is(genericData)
                .and("fileName").is(fileName)),
        		GenericFile.class);
    }
}
