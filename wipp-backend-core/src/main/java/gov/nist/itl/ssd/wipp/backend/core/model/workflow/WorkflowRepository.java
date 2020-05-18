/**
 * 
 */
package gov.nist.itl.ssd.wipp.backend.core.model.workflow;

import org.springframework.data.rest.core.annotation.RestResource;

import gov.nist.itl.ssd.wipp.backend.core.model.auth.PrincipalFilteredRepository;


/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 *
 */
public interface WorkflowRepository extends PrincipalFilteredRepository<Workflow, String> {
	
	@Override
    @RestResource(exported = false)
    void delete(Workflow w);
}
