package gov.nist.itl.ssd.wipp.backend.data.tensorboard;

import com.mongodb.lang.NonNull;
import gov.nist.itl.ssd.wipp.backend.data.PrincipalFilteredRepository;
import gov.nist.itl.ssd.wipp.backend.data.tensorflowmodels.TensorflowModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
*/
@RepositoryRestResource(path="tensorboardLogs")
public interface TensorboardLogsRepository extends PrincipalFilteredRepository<TensorboardLogs, String> {

	@Override
	@NonNull
	@RestResource(exported = false)
	// When calling the save method, which corresponds to a PUT/PATCH operation, we make sure that the user is logged in and has the right to access the object before calling the save method
	@PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorize(#s)")
	<S extends TensorboardLogs> S save(@NonNull @Param("s") S s);

	@Override
	@RestResource(exported = false)
	// When calling the delete method, which corresponds to a DELETE operation, we make sure that the user is logged in and has the right to access the object before calling the delete method
	@PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorize(#t)")
	void delete(@NonNull @Param("t") TensorboardLogs t);

	// The @Query annotation is used to exclude objects which should not be accessible by the user doing the request
	@Query("{'$or':[{'owner': ?#{ principal=='anonymousUser' ? '':principal.name}},{'publiclyAvailable':true}]}")
	TensorboardLogs findOneBySourceJob(@Param("sourceJob") String sourceJob);

	@Override
	// the findById method corresponds to a GET operation on a specific object. We can not use @PreAuthorize on the object's Id, as checkAuthorizeTensorboardLogsId() in SecurityServiceData
	// calls the findById method. Therefore, we use a @PostAuthorize on the object returned by the findById method. If the user is not allowed to GET the object, the object won't be
	// returned and an ForbiddenException will be thrown
	@PostAuthorize("@securityServiceData.checkAuthorize(returnObject.get())")
	@NonNull
	Optional<TensorboardLogs> findById(@NonNull String tensorboardLogsId);

	@Override
	// When calling the deletebyId method, which corresponds to a DELETE operation, we make sure that the user is logged in and has the right to access the object before calling the method
	// The checkAuthorizeTensorboardLogsId() method inside securityServiceData will retrieve the object before checking that the user has the right to access it
	@PreAuthorize("@securityServiceData.hasUserRole() and @securityServiceData.checkAuthorizeTensorboardLogsId(#s)")
	void deleteById(@NonNull @Param("s") String s);
}
