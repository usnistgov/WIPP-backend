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
package gov.nist.itl.ssd.wipp.backend.securityutils;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;


/**
 * A {@link WithUserDetailsSecurityContextFactory} that works with {@link WithMockKeycloakUser}.
 * 
 * Based on {@link WithMockUserSecurityContextFactory} using {@link KeycloakAuthenticationToken} 
 * instead of {@link UsernamePasswordAuthenticationToken}
 *
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class WithMockKeycloakUserSecurityContextFactory {
//	implements WithSecurityContextFactory<WithMockKeycloakUser> {

//	@Override
//	public SecurityContext createSecurityContext(WithMockKeycloakUser withUser) {
//		String username = StringUtils.hasLength(withUser.username()) ? withUser
//				.username() : withUser.value();
//		if (username == null) {
//			throw new IllegalArgumentException(withUser
//					+ " cannot have null username on both username and value properties");
//		}
//
//		List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
//		for (String authority : withUser.authorities()) {
//			grantedAuthorities.add(new SimpleGrantedAuthority(authority));
//		}
//
//		if (grantedAuthorities.isEmpty()) {
//			for (String role : withUser.roles()) {
//				if (role.startsWith("ROLE_")) {
//					throw new IllegalArgumentException("roles cannot start with ROLE_ Got "
//							+ role);
//				}
//				grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
//			}
//		} else if (!(withUser.roles().length == 1 && "USER".equals(withUser.roles()[0]))) {
//			throw new IllegalStateException("You cannot define roles attribute "+ Arrays.asList(withUser.roles())+" with authorities attribute "+ Arrays.asList(withUser.authorities()));
//		}
//
//		// Create mock KeycloakAccount for mockUser
//		RefreshableKeycloakSecurityContext securityContext = mock(RefreshableKeycloakSecurityContext.class);
//		AccessToken keycloakAccessToken = new AccessToken();
//		when(securityContext.getToken()).thenReturn(keycloakAccessToken);
//		Principal principal = new KeycloakPrincipal<KeycloakSecurityContext>(username, securityContext);
//		KeycloakAccount account = new SimpleKeycloakAccount(principal,
//				new HashSet<String>(Arrays.asList(withUser.roles())), securityContext);
//
//		// Create mock KeycloakAuthenticationToken and return SecurityContext containing token
//		Authentication authentication = new KeycloakAuthenticationToken(account, false, grantedAuthorities);
//		//when(authentication.getPrincipal()).thenReturn(principal);
//		SecurityContext context = SecurityContextHolder.createEmptyContext();
//		context.setAuthentication(authentication);
//		return context;
//	}

}
