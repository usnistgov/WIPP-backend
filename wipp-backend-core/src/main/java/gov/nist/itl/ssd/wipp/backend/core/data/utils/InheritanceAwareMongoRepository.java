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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.util.StreamUtils;
import org.springframework.data.util.Streamable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Workaround for https://jira.spring.io/browse/DATAMONGO-1142
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 * @param <T>
 * @param <ID>
 */
public class InheritanceAwareMongoRepository<T, ID>
        extends SimpleMongoRepository<T, ID> {

    private final MongoOperations mongoOperations;
    private final MongoEntityInformation<T, ID> entityInformation;

    public InheritanceAwareMongoRepository(
            MongoEntityInformation<T, ID> metadata,
            MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.mongoOperations = mongoOperations;
        this.entityInformation = metadata;
    }

    @Override
    public Optional<T> findById(ID id) {
        Assert.notNull(id, "The given id must not be null!");
        return Optional.ofNullable(mongoOperations.findOne(getIdQuery(id),
                entityInformation.getJavaType(),
                entityInformation.getCollectionName()));
    }

    private Query getIdQuery(Object id) {
        return new Query(getIdCriteria(id)).addCriteria(getClassCriteria());
    }

    private Criteria getIdCriteria(Object id) {
        return where(entityInformation.getIdAttribute()).is(id);
    }

    private Criteria getClassCriteria() {
        return where("_class").is(entityInformation.getJavaType().getTypeName());
    }

    @Override
    public boolean existsById(ID id) {
        Assert.notNull(id, "The given id must not be null!");
        return mongoOperations.exists(getIdQuery(id),
                entityInformation.getJavaType(),
                entityInformation.getCollectionName());
    }

    @Override
    public long count() {
        return mongoOperations.getCollection(
                entityInformation.getCollectionName()).count(
                        getClassCriteria().getCriteriaObject());
    }

    @Override
    public void deleteById(ID id) {
        Assert.notNull(id, "The given id must not be null!");
        mongoOperations.remove(getIdQuery(id),
                entityInformation.getJavaType(),
                entityInformation.getCollectionName());
    }

    @Override
    public void delete(T entity) {
        Assert.notNull(entity, "The given entity must not be null!");
        deleteById(entityInformation.getId(entity));
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        mongoOperations.remove(new Query(getClassCriteria()),
                entityInformation.getCollectionName());
    }

    @Override
    public List<T> findAll() {
        return findAll(new Query());
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
    	return findAll(new Query(new Criteria(entityInformation.getIdAttribute())
				.in(Streamable.of(ids).stream().collect(StreamUtils.toUnmodifiableList()))));
    }

    @Override
    public Page<T> findAll(final Pageable pageable) {

    	Assert.notNull(pageable, "Pageable must not be null!");
    	
        Long count = count();
        List<T> list = findAll(new Query().with(pageable));

        return new PageImpl<>(list, pageable, count);
    }

    @Override
    public List<T> findAll(Sort sort) {
    	
    	Assert.notNull(sort, "Sort must not be null!");
    	
        return findAll(new Query().with(sort));
    }

    private List<T> findAll(@Nullable Query query) {

        if (query == null) {
            return Collections.emptyList();
        }

     // Commented out because the CustomMongoTemplate will already add the criteria.
//      query.addCriteria(getClassCriteria());
        return mongoOperations.find(query, entityInformation.getJavaType(),
                entityInformation.getCollectionName());
    }

}
