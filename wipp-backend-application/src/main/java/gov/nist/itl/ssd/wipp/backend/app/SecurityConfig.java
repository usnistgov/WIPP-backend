package gov.nist.itl.ssd.wipp.backend.app;

import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;

/**
 * Keycloak/Spring security configuration
 */
@KeycloakConfiguration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter
{
    

    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }

    /**
     * Registers the KeycloakAuthenticationProvider with the authentication manager.
     * SimpleAuthorityMapper is used to make sure roles are not prefixed with ROLE_
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    	KeycloakAuthenticationProvider keycloakAuthenticationProvider
        = keycloakAuthenticationProvider();
       keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(
         new SimpleAuthorityMapper());
       auth.authenticationProvider(keycloakAuthenticationProvider);    
    }

    /**
     * Defines the session authentication strategy.
     */
    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    /**
     * Configures HTTP security
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
		super.configure(http);
		
		http
			.csrf().disable() 
			// restrict Create/Update/Delete operations to authenticated users
			.authorizeRequests()
				.antMatchers(HttpMethod.POST).authenticated()
				.antMatchers(HttpMethod.PUT).authenticated()
				.antMatchers(HttpMethod.PATCH).authenticated()
				.antMatchers(HttpMethod.DELETE).authenticated()
				.anyRequest().permitAll()
			// return 401 Unauthorized instead of 302 redirect to login page 
			// for unauthorized access by anonymous user
			.and()			
				.exceptionHandling()
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
    }
    
    /**
     * Exclude workflow exit controller from requiring authentication to allow Argo 
     * to POST workflow exit status
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(HttpMethod.POST, CoreConfig.BASE_URI + "/workflows/{workflowId}/exit");
    }
}