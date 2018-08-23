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

import java.io.Serializable;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import gov.nist.itl.ssd.wipp.backend.core.data.annotation.InheritedAwareRepository;

/**
 * Workaround for https://jira.spring.io/browse/DATAMONGO-1142
 * 
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * @param <T>
 * @param <S>
 * @param <ID>
 */
public class CustomMongoRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends MongoRepositoryFactoryBean<T, S, ID> {

    public CustomMongoRepositoryFactoryBean(
			Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	@Override
    protected RepositoryFactorySupport getFactoryInstance(MongoOperations operations) {
        return new CustomMongoRepositoryFactory(operations);
    }

    private static class CustomMongoRepositoryFactory extends MongoRepositoryFactory {

        private final MongoOperations mongoOperations;

        public CustomMongoRepositoryFactory(MongoOperations mongoOperations) {
            super(mongoOperations);
            this.mongoOperations = mongoOperations;
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        protected Object getTargetRepository(RepositoryInformation metadata) {
            Class<?> repositoryInterface = metadata.getRepositoryInterface();
            MongoEntityInformation<?, Serializable> entityInformation
                    = getEntityInformation(metadata.getDomainType());
            if (repositoryInterface.isAnnotationPresent(InheritedAwareRepository.class)) {
                return new InheritanceAwareMongoRepository(
                        entityInformation, mongoOperations);
            }
            return super.getTargetRepository(metadata);
        }

    }

}
