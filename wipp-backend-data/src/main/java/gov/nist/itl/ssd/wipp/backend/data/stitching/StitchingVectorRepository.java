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
package gov.nist.itl.ssd.wipp.backend.data.stitching;

import com.mongodb.lang.NonNull;
import gov.nist.itl.ssd.wipp.backend.data.PrincipalFilteredRepository;
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.CsvCollection;
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
* @author Antoine Vandecreme
*/
@RepositoryRestResource
public interface StitchingVectorRepository  extends PrincipalFilteredRepository<StitchingVector, String>, StitchingVectorRepositoryCustom {

	@Override
	// the findById method corresponds to a GET operation on a specific object. We can not use @PreAuthorize on the object's Id, as checkAuthorizeStitchingVectorId() in SecurityServiceData
	// calls the findById method. Therefore, we use a @PostAuthorize on the object returned by the findById method. If the user is not allowed to GET the object, the object won't be
	// returned and an ForbiddenException will be thrown
	@PostAuthorize("@securityServiceData.checkAuthorize(returnObject.get())")
	@NonNull
	Optional<StitchingVector> findById(@NonNull String stitchingVector);

	@Override
	@RestResource(exported = false)
	@NonNull
	// When calling the save method, which corresponds to a PUT/PATCH operation, we make sure that the user is logged in and has the right to access the object before calling the save method
	@PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorize(#s)")
	<S extends StitchingVector> S save(@NonNull @Param("s") S s);

	@Override
	@RestResource(exported = false)
	// When calling the delete method, which corresponds to a DELETE operation, we make sure that the user is logged in and has the right to access the object before calling the delete method
	@PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorize(#stitchingVector)")
	void delete(@NonNull @Param("stitchingVector") StitchingVector stitchingVector);


	long countByName(@Param("name") String name);

}
