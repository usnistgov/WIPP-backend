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
package gov.nist.itl.ssd.wipp.backend.data.visualization;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import gov.nist.itl.ssd.wipp.backend.Application;
import gov.nist.itl.ssd.wipp.backend.app.SecurityConfig;
import gov.nist.itl.ssd.wipp.backend.securityutils.WithMockKeycloakUser;

/**
 * Collection of tests for {@link VisualizationRepository} exposed methods
 * Testing access control on READ operations
 * Uses embedded MongoDB database and mock Keycloak users
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class, SecurityConfig.class }, 
				properties = { "spring.data.mongodb.port=0" })
public class VisualizationRepositoryTest {
	
	@Autowired WebApplicationContext context;
	@Autowired FilterChainProxy filterChain;

	MockMvc mvc;
	
	@Autowired
	VisualizationRepository visualizationRepository;
	
	Visualization publicVisualizationA, publicVisualizationB, privateVisualizationA, privateVisualizationB;
	
	@Before
	public void setUp() {
		this.mvc = webAppContextSetup(context)
				.apply(springSecurity())
				.addFilters(filterChain)
				.build();
		
		// Clear embedded database
		visualizationRepository.deleteAll();
		
		// Create and save publicVisualizationA (public: true, owner: user1)
		publicVisualizationA = new Visualization("publicVisualizationA");
		publicVisualizationA.setOwner("user1");
		publicVisualizationA.setPubliclyShared(true);
		publicVisualizationA = visualizationRepository.save(publicVisualizationA);
		// Create and save publicVisualizationB (public: true, owner: user2)
		publicVisualizationB = new Visualization("publicVisualizationB");
		publicVisualizationB.setOwner("user2");
		publicVisualizationB.setPubliclyShared(true);
		publicVisualizationB = visualizationRepository.save(publicVisualizationB);
		// Create and save privateVisualizationA (public: false, owner: user1)
		privateVisualizationA = new Visualization("privateVisualizationA");
		privateVisualizationA.setOwner("user1");
		privateVisualizationA.setPubliclyShared(false);
		privateVisualizationA = visualizationRepository.save(privateVisualizationA);
		// Create and save privateVisualizationB (public: false, owner: user2)
		privateVisualizationB = new Visualization("privateVisualizationB");
		privateVisualizationB.setOwner("user2");
		privateVisualizationB.setPubliclyShared(false);
		privateVisualizationB = visualizationRepository.save(privateVisualizationB);
	}
	
	@Test
	@WithAnonymousUser
	public void findById_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		// Anonymous user should be able to read a public visualization
		visualizationRepository.findById(publicVisualizationA.getId());
		
		// Anonymous user should not be able to read a private visualization
		try {
			visualizationRepository.findById(privateVisualizationA.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findById_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		// Non-admin user1 should be able to read own private visualization
		visualizationRepository.findById(privateVisualizationA.getId());
				
		// Non-admin user1 should be able to read a public visualization from user2
		visualizationRepository.findById(publicVisualizationB.getId());
		
		// Non-admin user1 should not be able to read a private visualization from user2
		try {
			visualizationRepository.findById(privateVisualizationB.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findById_adminCallingShouldReturnAllItems() throws Exception {
		
		// Admin should be able to read a public visualization from user1
		visualizationRepository.findById(publicVisualizationA.getId());
		
		// Admin should be able to read a private visualization from user1
		visualizationRepository.findById(privateVisualizationA.getId());
	}
	
	@Test
	@WithAnonymousUser
	public void findAll_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public visualizations
		Page<Visualization> result = visualizationRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(2));
		result.getContent().forEach(visualization -> {
			assertThat(visualization.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findAll_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public visualizations
		Page<Visualization> result = visualizationRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(visualization -> {
			assertThat((visualization.isPubliclyShared() || visualization.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findAll_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all visualizations
		Page<Visualization> result = visualizationRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(4));
	}
	
	@Test
	@WithAnonymousUser
	public void findByNameContainingIgnoreCase_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public visualizations matching search criteria
		Page<Visualization> result = visualizationRepository.findByNameContainingIgnoreCase("visualizationA", pageable);
		assertThat(result.getContent(), hasSize(1));
		result.getContent().forEach(visualization -> {
			assertThat(visualization.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findByNameContainingIgnoreCase_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public visualizations matching search criteria
		Page<Visualization> result = visualizationRepository.findByNameContainingIgnoreCase("visualization", pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(visualization -> {
			assertThat((visualization.isPubliclyShared() || visualization.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findByNameContainingIgnoreCase_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all visualizations matching search criteria
		Page<Visualization> resultColl = visualizationRepository.findByNameContainingIgnoreCase("visualization", pageable);
		assertThat(resultColl.getContent(), hasSize(4));
		Page<Visualization> resultPrivate = visualizationRepository.findByNameContainingIgnoreCase("private", pageable);
		assertThat(resultPrivate.getContent(), hasSize(2));
	}
	
}
