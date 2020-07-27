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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

/**
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 */
@RepositoryRestResource
public interface JobRepository<T extends Job> extends MongoRepository<T, String> {
    @Override
    void delete(T t);

    long countByName(@Param("name") String name);

    @RestResource(exported = false)
    List<T> findByStatus(JobStatus status);

    Page<T> findByStatus(@Param("status") JobStatus status, Pageable p);

    Page<T> findByNameContainingIgnoreCase(
            @Param("name") String name, Pageable p);

    Page<T> findByNameContainingIgnoreCaseAndStatus(@Param("name") String name,
                                                    @Param("status") String status, Pageable p);

    @RestResource(exported = false)
    List<T> findByWippWorkflow(String workflow);

    Page<T> findByWippWorkflow(@Param("wippWorkflow") String workflow,
                               Pageable p);

    List<T> findByWippWorkflowOrderByCreationDateAsc(@Param("wippWorkflow") String workflow);

}
