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
package gov.nist.itl.ssd.wipp.backend.core.model.job;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import gov.nist.itl.ssd.wipp.backend.core.model.job.WippJob;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * @param <T>
 */
@NoRepositoryBean
public interface BaseJobRepository<T extends WippJob>
        extends MongoRepository<T, String> {

    @Override
    @RestResource(exported = false)
    void delete(T t);

    @RestResource(exported = false)
    List<T> findByStatus(JobStatus status);

    Page<T> findByStatus(@Param("status") JobStatus status, Pageable p);

    Page<T> findByNameContainingIgnoreCase(
            @Param("name") String name, Pageable p);

    Page<T> findByNameContainingIgnoreCaseAndStatus(@Param("name") String name,
            @Param("status") String status, Pageable p);

}
