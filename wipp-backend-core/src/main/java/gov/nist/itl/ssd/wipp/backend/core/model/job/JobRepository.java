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

import com.mongodb.lang.NonNull;
import gov.nist.itl.ssd.wipp.backend.core.model.PrincipalFilteredRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.Workflow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

/**
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 */
@RepositoryRestResource

public interface JobRepository<T extends Job> extends PrincipalFilteredRepository<T, String> {

    @Override
    // When calling the delete method, which corresponds to a DELETE operation, we make sure that the user is logged in and has the right to access the object before calling the delete method
    @PreAuthorize("@securityServiceCore.hasUserRole() and @securityServiceCore.checkAuthorize(#t)")
    @RestResource(exported = false)
    void delete(@NonNull @Param("t") T t);

    long countByName(@Param("name") String name);

    @RestResource(exported = false)
    // The @Query annotation is used to exclude objects which should not be accessible by the user doing the request
    @Query(" { '$and' : [{'owner': ?#{ principal=='anonymousUser' ? '':principal.name}}, {'status' : {$eq : ?0}} ] }")
    List<T> findByStatus(JobStatus status);

    // The @Query annotation is used to exclude objects which should not be accessible by the user doing the request
    @Query(" { '$and' : [{'owner': ?#{ principal=='anonymousUser' ? '':principal.name}}, {'status' : {$eq : ?0}} ] }")
    Page<T> findByStatus(JobStatus status, Pageable p);

    // The @Query annotation is used to exclude objects which should not be accessible by the user doing the request
    @Query(" { '$and' : [{'owner': ?#{ principal=='anonymousUser' ? '':principal.name}}, {'name' : {$regex : '?0', $options: 'i'} } , {'status' : {$eq : ?1}} ] }")
    Page<T> findByNameContainingIgnoreCaseAndStatus(@Param("name") String name,
                                                    @Param("status") String status, Pageable p);

    // We make sure the user has access to the workflow before calling the findByWippWorkflow method
    @PreAuthorize("@securityServiceCore.checkAuthorizeWorkflowId(#wippWorkflow)")
    @RestResource(exported = false)
    List<T> findByWippWorkflow(@Param("wippWorkflow") String wippWorkflow);

    // We make sure the user has access to the workflow before calling the findByWippWorkflow method
    @PreAuthorize("@securityServiceCore.checkAuthorizeWorkflowId(#wippWorkflow)")
    Page<T> findByWippWorkflow(@Param("wippWorkflow") String wippWorkflow,
                               Pageable p);

    // We make sure the user has access to the workflow before calling the findByWippWorkflowOrderByCreationDateAsc method
    @PreAuthorize("@securityServiceCore.checkAuthorizeWorkflowId(#wippWorkflow)")
    List<T> findByWippWorkflowOrderByCreationDateAsc(@Param("wippWorkflow") String wippWorkflow);

    @Override
    // the findById method corresponds to a GET operation on a specific object. We can not use @PreAuthorize on the object's Id, as checkAuthorizeJobId() in SecurityServiceCore
    // calls the findById method. Therefore, we use a @PostAuthorize on the object returned by the findById method. If the user is not allowed to GET the object, the object won't be
    // returned and an ForbiddenException will be thrown
    @PostAuthorize("@securityServiceCore.checkAuthorize(returnObject.get())")
    @NonNull
    Optional<T> findById(@NonNull String jobId);

    @NonNull
    // When calling the save method, which corresponds to a PUT/PATCH operation, we make sure that the user is logged in and has the right to access the object before calling the save method
    @PreAuthorize("@securityServiceCore.hasUserRole() and @securityServiceCore.checkAuthorize(#t)")
    T save(@NonNull @Param("t") T t);

    @Override
    // When calling the deletebyId method, which corresponds to a DELETE operation, we make sure that the user is logged in and has the right to access the object before calling the method
    // The checkAuthorizeJobId() method inside securityServiceCore will retrieve the object before checking that the user has the right to access it
    @PreAuthorize("@securityServiceCore.hasUserRole() and @securityServiceCore.checkAuthorizeJobId(#s)")
    void deleteById(@NonNull @Param("s") String s);

}
