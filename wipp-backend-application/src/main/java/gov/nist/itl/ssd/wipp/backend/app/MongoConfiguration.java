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
package gov.nist.itl.ssd.wipp.backend.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.nist.itl.ssd.wipp.backend.core.utils.SecurityUtils;

/**
 * Automatic MongoDB index creation for all entities at startup.
 * @see https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mapping.index-creation
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Configuration
public class MongoConfiguration {
	
	@Autowired
    private MongoTemplate mongoTemplate;
	
	@EventListener(ContextRefreshedEvent.class)
	public void initIndicesAfterStartup() {
		
		// Load security context for system operations
		// Workaround for https://stackoverflow.com/questions/59363763/spring-security-data-with-index-issues
    	SecurityUtils.runAsSystem();
    	
		MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate
        .getConverter().getMappingContext();

		// consider only entities that are annotated with @Document
		mappingContext.getPersistentEntities()
                            .stream()
                            .filter(it -> it.isAnnotationPresent(Document.class))
                            .forEach(it -> {

                            	IndexOperations indexOps = mongoTemplate.indexOps(it.getType());
                            	IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);
                            	resolver.resolveIndexFor(it.getType()).forEach(indexOps::ensureIndex);
    	
                            });
		// Clear security context after system operations
        SecurityContextHolder.clearContext();
	}

}
