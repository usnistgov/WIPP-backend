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
package gov.nist.itl.ssd.wipp.backend.core.utils;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security Utils
 * See https://github.com/spring-projects/spring-data-examples/blob/master/rest/security/src/main/java/example/springdata/rest/security/SecurityUtils.java
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class SecurityUtils {

	/**
	 * Configures the Spring Security {@link SecurityContext} to be authenticated as the user with the given username and
	 * password as well as the given granted authorities.
	 * 
	 * Used for system operations.
	 * {@link SecurityContextHolder.clearContext} should be called afterwards.
	 *
	 */
	public static void runAsSystem() {

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("system", "system", AuthorityUtils.createAuthorityList("ROLE_admin")));
	}
}
