package gov.nist.itl.ssd.wipp.backend.data.tensorflowmodels.tensorboard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
*/
@RepositoryRestResource(path="tensorboardLogs")
public interface TensorboardLogsRepository extends MongoRepository<TensorboardLogs, String>{

	@Override
	@RestResource(exported = false)
	<S extends TensorboardLogs> S save(S s);

	@Override
	@RestResource(exported = false)
	void delete(TensorboardLogs t);
	
	TensorboardLogs findOneBySourceJob(@Param("sourceJob") String sourceJob);

	Page<TensorboardLogs> findByNameContainingIgnoreCase(
			@Param("name") String name, Pageable p);
}
