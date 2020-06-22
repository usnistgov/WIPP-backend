/**
 *
 */
package gov.nist.itl.ssd.wipp.backend.argo.workflows.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;


/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 *
 */
@RepositoryRestResource
public interface ArgoWorkflowRepository extends JpaRepository<ArgoWorkflow, Integer>{

	List<ArgoWorkflow> findByNameContainingIgnoreCase(@Param("name") String name);

	ArgoWorkflow findByName(@Param("name") String name);

	ArgoWorkflow findById(@Param("id") String id);

	List<ArgoWorkflow> findAll();

}
