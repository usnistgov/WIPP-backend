/**
 * NIST-developed software is provided by NIST as a public service. You may
 * use, copy, and distribute copies of the software in any medium, provided
 * that you keep intact this entire notice. You may improve, modify, and create
 * derivative works of the software or any portion of the software, and you may
 * copy and distribute such modifications or works. Modified works should carry
 * a notice stating that you changed the software and should note the date and
 * nature of any such change. Please explicitly acknowledge the National
 * Institute of Standards and Technology as the source of the software.
 *
 * NIST-developed software is expressly provided "AS IS." NIST MAKES NO
 * WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT, OR ARISING BY OPERATION OF
 * LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT, AND DATA ACCURACY. NIST
 * NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE
 * UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST
 * DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE
 * SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE
 * CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.
 *
 * You are solely responsible for determining the appropriateness of using and
 * distributing the software and you assume all risks associated with its use,
 * including but not limited to the risks and costs of program errors,
 * compliance with applicable laws, damage to or loss of data, programs or
 * equipment, and the unavailability or interruption of operation. This
 * software is not intended to be used in any situation where a failure could
 * cause risk of injury or damage to property. The software developed by NIST
 * employees is not subject to copyright protection within the United States.
 */
package gov.nist.itl.ssd.wipp.backend.data.aimodel;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.tensorboard.TensorboardLogs;
import gov.nist.itl.ssd.wipp.backend.data.tensorboard.TensorboardLogsRepository;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Component
@RepositoryEventHandler(AiModel.class)
public class AiModelEventHandler {

    @Autowired
    AiModelRepository aiModelRepository;

    @Autowired
    AiModelLogic aiModelLogic;

    @Autowired
    TensorboardLogsRepository tensorboardLogsRepository;

    @Autowired
    CoreConfig config;

    @PreAuthorize("isAuthenticated()")
    @HandleBeforeCreate
    public void handleBeforeCreate(AiModel aiModel) {
        throw new ClientException("Creation of AiModel via REST API is not allowed.");
    }

    @HandleBeforeSave
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #aiModel.owner == authentication.name)")
    public void handleBeforeSave(AiModel aiModel) {
    	// Assert data exists
        Optional<AiModel> result = aiModelRepository.findById(
                aiModel.getId());
        if (!result.isPresent()) {
            throw new NotFoundException("AiModel with id " + aiModel.getId() + " not found");
        }

        AiModel oldSv = result.get();
        
        // Source job cannot be changed
        if (!Objects.equals(
                aiModel.getSourceJob(),
        		oldSv.getSourceJob())) {
            throw new ClientException("Can not change source job.");
        }

        // Assert data name is unique
        if (!Objects.equals(aiModel.getName(), oldSv.getName())) {
            aiModelLogic.assertAiModelNameUnique(
                    aiModel.getName());
        }
        
        // A public data cannot become private
        if (oldSv.isPubliclyShared() && !aiModel.isPubliclyShared()){
            throw new ClientException("Can not change a public data to private.");
        }
                
        // Owner cannot be changed
        if (!Objects.equals(
                aiModel.getOwner(),
        		oldSv.getOwner())) {
            throw new ClientException("Can not change owner.");
        }
    }
    
    @HandleAfterSave
    public void handleAfterSave(AiModel aiModel) {
    	// If AiModel was made public, check for associated TensorboardLogs
    	if (aiModel.isPubliclyShared() && aiModel.getSourceJob() != null) {
    		TensorboardLogs tensorboardLogs = tensorboardLogsRepository.findOneBySourceJob(
                    aiModel.getSourceJob());
    		if (!tensorboardLogs.isPubliclyShared()) {
    			tensorboardLogs.setPubliclyShared(true);
    			tensorboardLogsRepository.save(tensorboardLogs);
    		}
    	}
    }
}
