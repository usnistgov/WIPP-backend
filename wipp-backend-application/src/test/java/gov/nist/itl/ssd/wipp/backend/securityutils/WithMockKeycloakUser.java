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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;


/**
 * This annotation can be added to a test method to emulate running 
 * with a mocked Keycloak user.
 * 
 * Based on {@link WithMockUser}
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
//@WithSecurityContext(factory = WithMockKeycloakUserSecurityContextFactory.class)
public @interface WithMockKeycloakUser {

	/**
	 * Convenience mechanism for specifying the username. The default is "user". If
	 * {@link #username()} is specified it will be used instead of {@link #value()}
	 * @return
	 */
	String value() default "user";

	/**
	 * The username to be used. Note that {@link #value()} is a synonym for
	 * {@link #username()}, but if {@link #username()} is specified it will take
	 * precedence.
	 * @return
	 */
	String username() default "";

	/**
	 * <p>
	 * The roles to use. The default is "USER". A {@link GrantedAuthority} will be created
	 * for each value within roles. Each value in roles will automatically be prefixed
	 * with "ROLE_". For example, the default will result in "ROLE_user" being used.
	 * </p>
	 * <p>
	 * If {@link #authorities()} is specified this property cannot be changed from the default.
	 * </p>
	 *
	 * @return
	 */
	String[] roles() default { "user" };
	
	/**
	 * <p>
	 * The authorities to use. A {@link GrantedAuthority} will be created for each value.
	 * </p>
	 *
	 * <p>
	 * If this property is specified then {@link #roles()} is not used. This differs from
	 * {@link #roles()} in that it does not prefix the values passed in automatically.
	 * </p>
	 *
	 * @return
	 */
	String[] authorities() default {};

	/**
	 * The password to be used. The default is "password".
	 * @return
	 */
	String password() default "password";
	
	/**
	 * Determines when the {@link SecurityContext} is setup. The default is before
	 * {@link TestExecutionEvent#TEST_METHOD} which occurs during
	 * {@link org.springframework.test.context.TestExecutionListener#beforeTestMethod(TestContext)}
	 * @return the {@link TestExecutionEvent} to initialize before
	 * @since 5.1
	 */
	@AliasFor(annotation = WithSecurityContext.class)
	TestExecutionEvent setupBefore() default TestExecutionEvent.TEST_METHOD;
}
