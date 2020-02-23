/**
 * 
 */
package gov.nist.itl.ssd.wipp.backend.core.model.workflow;

import com.mongodb.lang.NonNull;
import gov.nist.itl.ssd.wipp.backend.core.model.PrincipalFilteredRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;


/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 *
 */
public interface WorkflowRepository extends PrincipalFilteredRepository<Workflow, String> {
	@Override
	// the findById method corresponds to a GET operation on a specific object. We can not use @PreAuthorize on the object's Id, as checkAuthorizeWorkflowId() in SecurityServiceCore
	// calls the findById method. Therefore, we use a @PostAuthorize on the object returned by the findById method. If the user is not allowed to GET the object, the object won't be
	// returned and an ForbiddenException will be thrown
	@PostAuthorize("@securityServiceCore.checkAuthorize(returnObject.get())")
	@NonNull
	Optional<Workflow> findById(@NonNull String workflowId);

	@Override
	@NonNull
	// When calling the save method, which corresponds to a PUT/PATCH operation, we make sure that the user is logged in and has the right to access the object before calling the save method
	@PreAuthorize("@securityServiceCore.hasUserRole() and @securityServiceCore.checkAuthorize(#s)")
	<S extends Workflow> S save(@NonNull @Param("s") S s);

	@Override
	// When calling the delete method, which corresponds to a DELETE operation, we make sure that the user is logged in and has the right to access the object before calling the delete method
	@PreAuthorize("@securityServiceCore.hasUserRole() and @securityServiceCore.checkAuthorize(#workflow)")
	void delete(@NonNull @Param("workflow") Workflow workflow);

	@Override
	// When calling the deletebyId method, which corresponds to a DELETE operation, we make sure that the user is logged in and has the right to access the object before calling the method
	// The checkAuthorizeWorkflowId() method inside securityServiceCore will retrieve the object before checking that the user has the right to access it
	@PreAuthorize("@securityServiceCore.hasUserRole() and @securityServiceCore.checkAuthorizeWorkflowId(#s)")
	void deleteById(@NonNull @Param("s") String s);
}
