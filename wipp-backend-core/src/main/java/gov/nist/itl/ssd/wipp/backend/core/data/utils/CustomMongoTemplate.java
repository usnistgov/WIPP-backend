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
package gov.nist.itl.ssd.wipp.backend.core.data.utils;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.List;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import gov.nist.itl.ssd.wipp.backend.core.data.annotation.InheritedAwareEntity;

/**
 * Workaround for https://jira.spring.io/browse/DATAMONGO-1142
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class CustomMongoTemplate extends MongoTemplate {

    public CustomMongoTemplate(MongoDbFactory mongoDbFactory,
            MongoConverter converter) {
        super(mongoDbFactory, converter);
    }

    @Override
    public long count(Query query, Class<?> entityClass, String collectionName) {
        query = addClassCriteria(query, entityClass);
        return super.count(query, entityClass, collectionName);
    }

    @Override
    public <T> List<T> find(Query query, Class<T> entityClass, String collectionName) {
        query = addClassCriteria(query, entityClass);
        return super.find(query, entityClass, collectionName);
    }

    private <T> Query addClassCriteria(Query query, Class<T> entityClass) {
        if (entityClass.isAnnotationPresent(InheritedAwareEntity.class)) {
            if (query == null) {
                query = new Query();
            }
            query.restrict(entityClass);
        }
        return query;
    }

}

