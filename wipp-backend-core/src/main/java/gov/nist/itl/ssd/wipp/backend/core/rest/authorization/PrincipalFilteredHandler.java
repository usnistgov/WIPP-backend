package gov.nist.itl.ssd.wipp.backend.core.rest.authorization;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * @author Antoine Gerardin <antoine.gerardin@nist.gov> @2017
 *
 * @preAuthorize annotations on generics are not currently picked up by the AOP
 * mecanism used by Spring Security.
 * Thus we cannot factorize the user access capability.
 * https://github.com/spring-projects/spring-security/issues/3286
 *
 * We keep this class in order to document the problem properly (until a fix?)
 */
public interface PrincipalFilteredHandler<T> {

    //Useless has generics are ignored by Spring Security AOP.
    @PreAuthorize("#imagesCollection.owner == principal.username")
    public void handleBeforeDelete(T object);

}
