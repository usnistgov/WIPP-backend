// todo
package gov.nist.itl.ssd.wipp.backend.data.aimodelcard;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * AI model card Security service
 *
 * @author Antoine Meyer <antoine.meyer at nist.gov>
 *
 */
@Service
public class AiModelCardSecurity {

    @Autowired
    private AiModelCardRepository aiModelCardRepository;

    public boolean checkAuthorize(String aiModelCardId, Boolean editMode) {
        Optional<AiModelCard> aiModelCard = aiModelCardRepository.findById(aiModelCardId);
        if(aiModelCard.isPresent()) {
            System.out.println("YES");
            return(checkAuthorize(aiModelCard.get(), editMode));
        } else {
            System.out.println("NO");
            throw new NotFoundException("AI model card with id " + aiModelCardId + " not found");
        }
    }

    public static boolean checkAuthorize(AiModelCard aiModelCard, Boolean editMode) {
        String aiModelCardOwner = aiModelCard.getOwner();
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("YES 2");
        if(!aiModelCard.isPubliclyShared() && (aiModelCardOwner == null || !aiModelCardOwner.equals(connectedUser))) {
            System.out.println("NO 2");
            throw new ForbiddenException("You do not have access to this AI model card");
        }
        if (aiModelCard.isPubliclyShared() && editMode && (aiModelCardOwner == null || !aiModelCardOwner.equals(connectedUser))){
            System.out.println("NO 3");
            throw new ForbiddenException("You do not have the right to edit this AI model card");
        }
        return(true);
    }

}
