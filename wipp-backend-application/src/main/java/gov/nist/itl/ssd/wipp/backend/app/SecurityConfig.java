package gov.nist.itl.ssd.wipp.backend.app;

import java.util.*;

import com.jayway.jsonpath.JsonPath;
import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;


import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.stream.Collectors;

/**
 * Keycloak/Spring security configuration
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
public class SecurityConfig
{

	private static final String GROUPS = "groups";
	private static final String REALM_ACCESS_CLAIM = "realm_access";
	private static final String ROLES_CLAIM = "roles";

    /**
     * Register the KeycloakAuthenticationProvider with the authentication manager.
     * SimpleAuthorityMapper is used to make sure roles are not prefixed with ROLE_
     */
 //   @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//    	KeycloakAuthenticationProvider keycloakAuthenticationProvider
//        = keycloakAuthenticationProvider();
//       keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(
//         new SimpleAuthorityMapper());
//       auth.authenticationProvider(keycloakAuthenticationProvider);
//    }
	
    /**
     * Use Spring Security expressions in Spring Data queries
     * @return
     */
    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }

    /**
     * Define the session authentication strategy.
     * NullAuthenticatedSessionStrategy for bearer-only applications
     */
    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    /**
     * Configure HTTP security
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter) throws Exception
    {
		// Configure app as OAuth2 resource server with custom Keycloak JWT converter
		http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

		// Disable CSRF for bearer-only applications
		http.csrf(csrf -> csrf.disable());

		// Stateless session for bearer-only applications
		http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// restrict Create/Update/Delete operations to authenticated users
		http.authorizeHttpRequests(authz -> authz
				.requestMatchers(HttpMethod.POST).authenticated()
				.requestMatchers(HttpMethod.PUT).authenticated()
				.requestMatchers(HttpMethod.PATCH).authenticated()
				.requestMatchers(HttpMethod.DELETE).authenticated()
				// restrict wdzt pyramid files access to users authorized to access the pyramid
				.requestMatchers(CoreConfig.PYRAMIDS_BASE_URI + "/{pyramidId}/**")
					.access(new WebExpressionAuthorizationManager("hasRole('admin') or @pyramidSecurity.checkAuthorize(#pyramidId, false)"))
				// allow other GET/OPTIONS requests, fine grain access control done at method level
				.anyRequest().permitAll()
		);

		// return 401 Unauthorized instead of 302 redirect to login page
		// for unauthorized access by anonymous user
		http.exceptionHandling(eh -> eh.authenticationEntryPoint((request, response, authException) -> {
			response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
		}));

		// Enable CORS
		http.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()));

        return http.build();
    }

	/**
	 * Keycloak JWT auth token converter (custom role and username mapping for Keycloak)
	 */
	@Component
	static class KeycloakJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {
		@Override
		public JwtAuthenticationToken convert(Jwt jwt) {
			final var authorities = new KeyclaokJwtGrantedAuthoritiesConverter().convert(jwt);
			final String username = JsonPath.read(jwt.getClaims(), "preferred_username");
			return new JwtAuthenticationToken(jwt, authorities, username);
		}
	}

	/**
	 * Keycloak JWT granted authorities converter (custom Keycloak -> Spring security role mapping)
	 */
	static class KeyclaokJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<? extends GrantedAuthority>> {
		@Override
		public Collection<GrantedAuthority> convert(Jwt jwt) {
			var realmAccess = (Map<String, List<String>>) jwt.getClaim("realm_access");

			return realmAccess.get("roles").stream()
					.map(role -> new SimpleGrantedAuthority("ROLE_" + role))
					.collect(Collectors.toList());
		}
	}

    /**
     * Exclude workflow exit controller from requiring authentication to allow Argo 
     * to POST workflow exit status
     */
	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers(HttpMethod.POST, CoreConfig.BASE_URI + "/workflows/{workflowId}/exit");
	}

	/** Enable CORS for all requests **/
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setMaxAge(Long.valueOf(3600));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "x-requested-with", "Content-Type"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}