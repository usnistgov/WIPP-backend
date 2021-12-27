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
package gov.nist.itl.ssd.wipp.backend.data.pyramid;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
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
 * Collection of tests for {@link PyramidRepository} exposed methods
 * Testing access control on READ operations
 * Uses embedded MongoDB database and mock Keycloak users
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class, SecurityConfig.class }, 
				properties = { "spring.data.mongodb.port=0" })
public class PyramidRepositoryTest {
	
	@Autowired WebApplicationContext context;
	@Autowired FilterChainProxy filterChain;

	MockMvc mvc;
	
	@Autowired
	PyramidRepository pyramidRepository;
	
	Pyramid publicPyramidA, publicPyramidB, privatePyramidA, privatePyramidB;
	
	@Before
	public void setUp() {
		this.mvc = webAppContextSetup(context)
				.apply(springSecurity())
				.addFilters(filterChain)
				.build();
		
		// Clear embedded database
		pyramidRepository.deleteAll();
		
		// Create and save publicPyramidA (public: true, owner: user1)
		publicPyramidA = new Pyramid("publicPyramidA");
		publicPyramidA.setOwner("user1");
		publicPyramidA.setPubliclyShared(true);
		publicPyramidA = pyramidRepository.save(publicPyramidA);
		// Create and save publicPyramidB (public: true, owner: user2)
		publicPyramidB = new Pyramid("publicPyramidB");
		publicPyramidB.setOwner("user2");
		publicPyramidB.setPubliclyShared(true);
		publicPyramidB = pyramidRepository.save(publicPyramidB);
		// Create and save privatePyramidA (public: false, owner: user1)
		privatePyramidA = new Pyramid("privatePyramidA");
		privatePyramidA.setOwner("user1");
		privatePyramidA.setPubliclyShared(false);
		privatePyramidA = pyramidRepository.save(privatePyramidA);
		// Create and save privatePyramidB (public: false, owner: user2)
		privatePyramidB = new Pyramid("privatePyramidB");
		privatePyramidB.setOwner("user2");
		privatePyramidB.setPubliclyShared(false);
		privatePyramidB = pyramidRepository.save(privatePyramidB);
	}
	
	@Test
	@WithAnonymousUser
	public void findById_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		// Anonymous user should be able to read a public pyramid
		pyramidRepository.findById(publicPyramidA.getId());
		
		// Anonymous user should not be able to read a private pyramid
		try {
			pyramidRepository.findById(privatePyramidA.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findById_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		// Non-admin user1 should be able to read own private pyramid
		pyramidRepository.findById(privatePyramidA.getId());
				
		// Non-admin user1 should be able to read a public pyramid from user2
		pyramidRepository.findById(publicPyramidB.getId());
		
		// Non-admin user1 should not be able to read a private pyramid from user2
		try {
			pyramidRepository.findById(privatePyramidB.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findById_adminCallingShouldReturnAllItems() throws Exception {
		
		// Admin should be able to read a public pyramid from user1
		pyramidRepository.findById(publicPyramidA.getId());
		
		// Admin should be able to read a private pyramid from user1
		pyramidRepository.findById(privatePyramidA.getId());
	}
	
	@Test
	@WithAnonymousUser
	public void findAll_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public pyramids
		Page<Pyramid> result = pyramidRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(2));
		result.getContent().forEach(pyramid -> {
			assertThat(pyramid.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findAll_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public pyramids
		Page<Pyramid> result = pyramidRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(pyramid -> {
			assertThat((pyramid.isPubliclyShared() || pyramid.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findAll_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all pyramids
		Page<Pyramid> result = pyramidRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(4));
	}
	
	@Test
	@WithAnonymousUser
	public void findByNameContainingIgnoreCase_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public pyramids matching search criteria
		Page<Pyramid> result = pyramidRepository.findByNameContainingIgnoreCase("pyramidA", pageable);
		assertThat(result.getContent(), hasSize(1));
		result.getContent().forEach(pyramid -> {
			assertThat(pyramid.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findByNameContainingIgnoreCase_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public pyramids matching search criteria
		Page<Pyramid> result = pyramidRepository.findByNameContainingIgnoreCase("pyramid", pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(pyramid -> {
			assertThat((pyramid.isPubliclyShared() || pyramid.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findByNameContainingIgnoreCase_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all pyramids matching search criteria
		Page<Pyramid> resultColl = pyramidRepository.findByNameContainingIgnoreCase("pyramid", pageable);
		assertThat(resultColl.getContent(), hasSize(4));
		Page<Pyramid> resultPrivate = pyramidRepository.findByNameContainingIgnoreCase("private", pageable);
		assertThat(resultPrivate.getContent(), hasSize(2));
	}
	
}
