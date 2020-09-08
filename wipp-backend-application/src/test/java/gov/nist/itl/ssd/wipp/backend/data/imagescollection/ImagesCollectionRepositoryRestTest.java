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
package gov.nist.itl.ssd.wipp.backend.data.imagescollection;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.nist.itl.ssd.wipp.backend.Application;
import gov.nist.itl.ssd.wipp.backend.app.SecurityConfig;
import gov.nist.itl.ssd.wipp.backend.securityutils.WithMockKeycloakUser;

/**
 * <h1>Collection of tests for {@link ImagesCollectionRepositoryEventHandler}</h1>
 * <p>
 * Testing access control on CREATE, UPDATE, DELETE operations for 
 * {@link ImagesCollectionRepository} with REST access
 * <p>
 * Default supported HTTP methods for Spring Data REST repositories are:
 * <ul>
 * 	<li>GET and POST for the Collection Resource (/{collectionName})</li>
 * 	<li>GET, PUT, PATCH and DELETE for the Item Resource (/{collectionName}/{itemId})</li>
 * 	<li>GET for the Search Resource (/{collectionName}/search/findBy...)</li>
 * </ul>
 * Uses embedded MongoDB database and mock Keycloak users
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = { Application.class, SecurityConfig.class }, 
				properties = { "spring.data.mongodb.port=0" })
public class ImagesCollectionRepositoryRestTest {

	static final String PAYLOAD = "{\"name\": \"test-coll\"}";
	
	@Autowired WebApplicationContext context;

	MockMvc mvc;
	
	@Autowired
	ImagesCollectionRepository imagesCollectionRepository;
	
	ImagesCollection publicCollA, publicCollB, privateCollA, privateCollB;
	
	@Before
	public void setUp() {
		mvc = webAppContextSetup(context)
				.apply(springSecurity())
				.build();
		
		// Clear embedded database
		imagesCollectionRepository.deleteAll();
		
		// Create and save publicCollA (public: true, owner: user1)
		publicCollA = new ImagesCollection("publicCollA");
		publicCollA.setOwner("user1");
		publicCollA.setPubliclyShared(true);
		publicCollA = imagesCollectionRepository.save(publicCollA);
		// Create and save publicCollB (public: true, owner: user2)
		publicCollB = new ImagesCollection("publicCollB");
		publicCollB.setOwner("user2");
		publicCollB.setPubliclyShared(true);
		publicCollB = imagesCollectionRepository.save(publicCollB);
		// Create and save privateCollA (public: false, owner: user1)
		privateCollA = new ImagesCollection("privateCollA");
		privateCollA.setOwner("user1");
		privateCollA.setPubliclyShared(false);
		privateCollA = imagesCollectionRepository.save(privateCollA);
		// Create and save privateCollB (public: false, owner: user2)
		privateCollB = new ImagesCollection("privateCollB");
		privateCollB.setOwner("user2");
		privateCollB.setPubliclyShared(false);
		privateCollB = imagesCollectionRepository.save(privateCollB);
	}
	
	@Test
	@WithAnonymousUser
	public void rejectsPostRequestsToCollectionResourceForAnonymousUser() throws Exception {
		
		mvc.perform(post("/api/imagesCollections")
				.content(PAYLOAD)
				.accept(MediaTypes.HAL_JSON))
				.andExpect(status().isUnauthorized());
	}
	
	@Test
	@WithAnonymousUser
	public void allowsGetRequestsToCollectionResourceForAnonymousUser() throws Exception {
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON_VALUE);
		mvc.perform(get("/api/imagesCollections")
				.headers(headers))
				.andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithAnonymousUser
	public void rejectsGetRequestsToPrivateItemResourceForAnonymousUser() throws Exception {
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON_VALUE);
		mvc.perform(get("/api/imagesCollections/" + privateCollA.getId())
				.headers(headers))
				.andExpect(status().isUnauthorized());
	}
	
	@Test
	@WithAnonymousUser
	public void allowsGetRequestsToPublicItemResourceForAnonymousUser() throws Exception {
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON_VALUE);
		mvc.perform(get("/api/imagesCollections/" + publicCollA.getId())
				.headers(headers))
				.andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
				.andExpect(status().isOk());
	}
	
//	@Test
//	@WithMockKeycloakUser(username="user1", roles={ "user" })
//	//@WithMockUser
//	public void allowsPostRequestsToCollectionResourceForAuthenticatedUser() throws Exception {
//		System.out.println(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
//		System.out.println(this.mvc.toString());
//		System.out.println(SecurityContextHolder.getContext().getAuthentication().toString());
////		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
////		token.getPrincipal().
////        AccessToken accessToken = token.getToken();
//		HttpHeaders headers = new HttpHeaders();
//		headers.add(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON_VALUE);
//		//headers.add(HttpHeaders.AUTHORIZATION);
//		KeycloakPrincipal<KeycloakSecurityContext> principal = (KeycloakPrincipal<KeycloakSecurityContext>) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//		String responseContent = mvc.perform(post("/api/imagesCollections")
//				//.with(securityContext(SecurityContextHolder.getContext()))
//				//.with(testSecurityContext())
//				//.with(opaqueToken())
//				.header("Authorization", "Bearer " + principal.getKeycloakSecurityContext().getIdTokenString())
//				.content(PAYLOAD)
//				.accept(MediaTypes.HAL_JSON))
//				.andExpect(status().isCreated())
//				.andReturn().getResponse().getContentAsString();
//		System.out.println(responseContent);
//		ObjectMapper mapper = new ObjectMapper();
//		ImagesCollection testColl = mapper.readValue(responseContent, ImagesCollection.class);
//		assertThat(testColl.getName(), is("test-coll"));
//		
//		imagesCollectionRepository.delete(testColl);
//	}
	
}
