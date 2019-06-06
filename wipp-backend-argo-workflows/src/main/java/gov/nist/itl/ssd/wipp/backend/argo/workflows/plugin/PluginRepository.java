package gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;


/**
 *
 *
 * @author Philippe Dessauw <philippe.dessauw at nist.gov>
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
@RepositoryRestResource
public interface PluginRepository extends MongoRepository<Plugin, String> {
    @Query("{}")
    List<Plugin> findCompleteList();
    
	Page<Plugin> findByNameContainingIgnoreCase(@Param("name") String name, Pageable p);
}
