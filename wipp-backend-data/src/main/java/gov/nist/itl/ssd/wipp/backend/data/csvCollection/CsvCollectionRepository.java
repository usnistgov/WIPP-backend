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
package gov.nist.itl.ssd.wipp.backend.data.csvCollection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@RepositoryRestResource
public interface CsvCollectionRepository extends MongoRepository<CsvCollection, String>{

	@Override
	@RestResource(exported = false)
	<S extends CsvCollection> S save(S s);

	@Override
	@RestResource(exported = false)
	void delete(CsvCollection t);

	Page<CsvCollection> findByNameContainingIgnoreCase(
			@Param("name") String name, Pageable p);

	long countByName(@Param("name") String name);
}
