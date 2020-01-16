package gov.nist.itl.ssd.wipp.backend.core.rest.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Created by gerardin on 4/6/17.
 */
public class AuthorizationVerifier {

    public static boolean  isOwnedByAuthenticatedUser(Owned object){
        //user need to own this collection
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return object.getOwner().equals(auth.getName());
    }
}
