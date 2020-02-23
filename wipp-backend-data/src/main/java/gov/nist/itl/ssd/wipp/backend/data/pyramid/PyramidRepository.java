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
package gov.nist.itl.ssd.wipp.backend.data.pyramid;

import com.mongodb.lang.NonNull;
import gov.nist.itl.ssd.wipp.backend.data.PrincipalFilteredRepository;
import gov.nist.itl.ssd.wipp.backend.data.tensorflowmodels.TensorflowModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;

/**
*
* @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
*/
@RepositoryRestResource
public interface PyramidRepository extends PrincipalFilteredRepository<Pyramid, String> {

    @Override
    @NonNull
    @RestResource(exported = false)
    // When calling the save method, which corresponds to a PUT/PATCH operation, we make sure that the user is logged in and has the right to access the object before calling the save method
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorize(#s, true)")
    <S extends Pyramid> S save(@NonNull @Param("s") S s);

    @Override
    @RestResource(exported = false)
    // When calling the delete method, which corresponds to a DELETE operation, we make sure that the user is logged in and has the right to access the object before calling the delete method
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorize(#t, true)")
    void delete(@NonNull @Param("t") Pyramid t);

    @Override
    // the findById method corresponds to a GET operation on a specific object. We can not use @PreAuthorize on the object's Id, as checkAuthorizePyramidId() in SecurityServiceData
    // calls the findById method. Therefore, we use a @PostAuthorize on the object returned by the findById method. If the user is not allowed to GET the object, the object won't be
    // returned and an ForbiddenException will be thrown
    @PostAuthorize("@securityServiceData.checkAuthorize(returnObject.get(), false)")
    @NonNull
    Optional<Pyramid> findById(@NonNull String pyramidId);

    @Override
    // When calling the deletebyId method, which corresponds to a DELETE operation, we make sure that the user is logged in and has the right to access the object before calling the method
    // The checkAuthorizePyramidId() method inside securityServiceData will retrieve the object before checking that the user has the right to access it
    @PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizePyramidId(#s, true)")
    void deleteById(@NonNull @Param("s") String s);

}
