/*
 * This software was developed at the National Institute of Standards and
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
 */

package gov.nist.itl.ssd.wipp.backend.core.model.workflow;


import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Workflow copy controller 
 * 
 * @author Samia Benjida <samia.benjida at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@RestController
@Tag(name="Workflow Entity")
@RequestMapping(CoreConfig.BASE_URI + "/workflows/{workflowId}/copy")
public class WorkflowCopyController
{
    @Autowired
    private WorkflowCopyService workflowCopyService;

    @PreAuthorize("isAuthenticated() and "
    		+ "(hasRole('admin') or @workflowSecurity.checkAuthorize(#workflowId, false))")
    @RequestMapping(
            value = "",
            method = RequestMethod.POST)  
    public EntityModel<Workflow> copy(
            @PathVariable("workflowId") String workflowId,
            @RequestBody String workflowName) {
    	
    	if (workflowName == null) {
            throw new ClientException(
                    "A name for the new workflow must be provided.");
        }

        try {
            // Set the owner to the connected user
            String copyWorkflowOwner = SecurityContextHolder.getContext().getAuthentication().getName();
            // Copy workflow
            Workflow copy = workflowCopyService.copy(workflowId, workflowName, copyWorkflowOwner, WorkflowStatus.CREATED);
            return EntityModel.of(copy);

        } catch (Exception e) {
            throw new ClientException("Error while copying workflow" + e);
        }
    }

}
