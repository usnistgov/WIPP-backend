/**
 * 
 */
package gov.nist.itl.ssd.wipp.backend.core.model.workflow;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;


/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 *
 */
public interface WorkflowRepository extends MongoRepository<Workflow, String> {

	Page<Workflow> findByNameContainingIgnoreCase(@Param("name") String name, Pageable p);

}
