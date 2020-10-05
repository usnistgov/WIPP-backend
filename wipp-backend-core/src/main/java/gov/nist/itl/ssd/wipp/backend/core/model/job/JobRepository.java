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

import gov.nist.itl.ssd.wipp.backend.core.model.auth.PrincipalFilteredRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@RepositoryRestResource

public interface JobRepository<T extends Job> extends PrincipalFilteredRepository<T, String> {

	@Override
    void delete(T t);

    long countByName(@Param("name") String name);

    @RestResource(exported = false)
    List<Job> findByStatus(JobStatus status);

    /*
	 * Filter collection resources access by object status depending on user
	 */
	@Query(" { '$and' : ["
			+ "{'$or':["
			+ "{'owner': ?#{ hasRole('admin') ? {$exists:true} : (hasRole('ANONYMOUS') ? '':principal.name)}},"
			+ "{'publiclyShared':true}"
			+ "]} , "
			+ "{'status' : {$eq : ?0}}"
			+ "]}")
    Page<T> findByStatus(@Param("status") JobStatus status, Pageable p);

	/*
	 * Filter collection resources access by name, status and depending on user
	 */
	@Query(" { '$and' : ["
    		+ "{'$or':["
    		+ "{'owner': ?#{ hasRole('admin') ? {$exists:true} : (hasRole('ANONYMOUS') ? '':principal.name)}},"
    		+ "	{'publiclyShared':true}"
    		+ "]} , "
    		+ "{'name' : {$regex : '?0', $options: 'i'}}, {'status' : {$eq : ?1}}"
    		+ "]}")
    Page<T> findByNameContainingIgnoreCaseAndStatus(@Param("name") String name,
                                                    @Param("status") String status, Pageable p);

    /*
     * Check user is authorized to access workflow before retrieving jobs
     */
    @PreAuthorize("hasRole('admin') or @workflowSecurity.checkAuthorize(#wippWorkflow, false)")
    Page<T> findByWippWorkflow(@Param("wippWorkflow") String workflow,
                               Pageable p);

    /*
     * Check user is authorized to access workflow before retrieving jobs
     */
    @PreAuthorize("hasRole('admin') or @workflowSecurity.checkAuthorize(#wippWorkflow, false)")
    List<T> findByWippWorkflowOrderByCreationDateAsc(@Param("wippWorkflow") String workflow);
    
    @RestResource(exported = false)
    List<Job> findByWippWorkflow(String workflow);
    
    @RestResource(exported = false)
    Long deleteByWippWorkflow(String workflow);
    
}
