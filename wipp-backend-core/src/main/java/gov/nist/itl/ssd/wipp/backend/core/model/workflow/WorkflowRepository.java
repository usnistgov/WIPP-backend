/**
 * 
 */
package gov.nist.itl.ssd.wipp.backend.core.model.workflow;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import gov.nist.itl.ssd.wipp.backend.core.model.auth.PrincipalFilteredRepository;


/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 *
 */
@Tag(name="Workflow Entity")
public interface WorkflowRepository extends PrincipalFilteredRepository<Workflow, String> {
	
	@Override
    @RestResource(exported = false)
    void delete(Workflow w);
	
	// Not exported
    long countByName(@Param("name") String name);
}
