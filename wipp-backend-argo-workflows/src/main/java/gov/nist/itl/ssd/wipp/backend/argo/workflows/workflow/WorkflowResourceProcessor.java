package gov.nist.itl.ssd.wipp.backend.argo.workflows.workflow;

import gov.nist.itl.ssd.wipp.backend.core.model.workflow.Workflow;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

/**
 *
 * @author Philippe Dessauw <philippe.dessauw at nist.gov>
 */
@Component
public class WorkflowResourceProcessor
    implements ResourceProcessor<Resource<Workflow>> {

    @Override
    public Resource<Workflow> process(Resource<Workflow> resource) {
        Workflow workflow = resource.getContent();

        Link submitLink = ControllerLinkBuilder.linkTo(
            WorkflowSubmitController.class, workflow.getId())
            .withRel("submit");
        resource.add(submitLink);

        return resource;
    }
}
