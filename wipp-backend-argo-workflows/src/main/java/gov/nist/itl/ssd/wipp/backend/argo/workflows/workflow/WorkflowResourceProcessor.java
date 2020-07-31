package gov.nist.itl.ssd.wipp.backend.argo.workflows.workflow;

import gov.nist.itl.ssd.wipp.backend.core.model.workflow.Workflow;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

/**
 *
 * @author Philippe Dessauw <philippe.dessauw at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Component
public class WorkflowResourceProcessor
    implements RepresentationModelProcessor<EntityModel<Workflow>> {

    @Override
    public EntityModel<Workflow> process(EntityModel<Workflow> resource) {
        Workflow workflow = resource.getContent();

        Link submitLink = WebMvcLinkBuilder.linkTo(
            WorkflowSubmitController.class, workflow.getId())
            .withRel("submit");
        resource.add(submitLink);

        Link exitLink = WebMvcLinkBuilder.linkTo(
                WorkflowExitController.class, workflow.getId())
                .withRel("exit");
            resource.add(exitLink);

        return resource;
    }
}
