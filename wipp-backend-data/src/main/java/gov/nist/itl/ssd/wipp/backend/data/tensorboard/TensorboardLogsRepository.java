package gov.nist.itl.ssd.wipp.backend.data.tensorboard;

import gov.nist.itl.ssd.wipp.backend.core.model.auth.PrincipalFilteredRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
* @author Mylene Simon <mylene.simon at nist.gov>
*/
@Tag(name="TensorboardLogs Entity")
@RepositoryRestResource(path="tensorboardLogs")
public interface TensorboardLogsRepository extends PrincipalFilteredRepository<TensorboardLogs, String> {

	@Override
	@RestResource(exported = false)
	<S extends TensorboardLogs> S save(S s);

	@Override
	@RestResource(exported = false)
	void delete(TensorboardLogs t);

	@PostAuthorize("hasRole('admin') "
			+ "or (isAuthenticated() and returnObject?.owner == authentication.name) "
			+ "or returnObject?.publiclyShared == true")
	TensorboardLogs findOneBySourceJob(@Param("sourceJob") String sourceJob);

}
