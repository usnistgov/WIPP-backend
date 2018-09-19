/**
 * 
 */
package gov.nist.itl.ssd.wipp.backend.core.model.workflow;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public interface WorkflowRepository extends MongoRepository<Workflow, String> {


}
