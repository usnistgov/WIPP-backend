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
package gov.nist.itl.ssd.wipp.backend.data.stitching;

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
 * Collection of tests for {@link StitchingVectorRepository} exposed methods
 * Testing access control on READ operations
 * Uses embedded MongoDB database and mock Keycloak users
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class, SecurityConfig.class }, 
				properties = { "spring.data.mongodb.port=0", 
						"spring.data.mongodb.auto-index-creation=false" })
public class StitchingVectorRepositoryTest {
	
	@Autowired WebApplicationContext context;
	@Autowired FilterChainProxy filterChain;

	MockMvc mvc;
	
	@Autowired
	StitchingVectorRepository stitchingVectorRepository;
	
	StitchingVector publicStitchingVectorA, publicStitchingVectorB, privateStitchingVectorA, privateStitchingVectorB;
	
	@Before
	public void setUp() {
		this.mvc = webAppContextSetup(context)
				.apply(springSecurity())
				.addFilters(filterChain)
				.build();
		
		// Clear embedded database
		stitchingVectorRepository.deleteAll();
		
		// Create and save publicStitchingVectorA (public: true, owner: user1)
		publicStitchingVectorA = new StitchingVector("publicStitchingVectorA", null, null);
		publicStitchingVectorA.setOwner("user1");
		publicStitchingVectorA.setPubliclyShared(true);
		publicStitchingVectorA = stitchingVectorRepository.save(publicStitchingVectorA);
		// Create and save publicStitchingVectorB (public: true, owner: user2)
		publicStitchingVectorB = new StitchingVector("publicStitchingVectorB", null, null);
		publicStitchingVectorB.setOwner("user2");
		publicStitchingVectorB.setPubliclyShared(true);
		publicStitchingVectorB = stitchingVectorRepository.save(publicStitchingVectorB);
		// Create and save privateStitchingVectorA (public: false, owner: user1)
		privateStitchingVectorA = new StitchingVector("privateStitchingVectorA", null, null);
		privateStitchingVectorA.setOwner("user1");
		privateStitchingVectorA.setPubliclyShared(false);
		privateStitchingVectorA = stitchingVectorRepository.save(privateStitchingVectorA);
		// Create and save privateStitchingVectorB (public: false, owner: user2)
		privateStitchingVectorB = new StitchingVector("privateStitchingVectorB", null, null);
		privateStitchingVectorB.setOwner("user2");
		privateStitchingVectorB.setPubliclyShared(false);
		privateStitchingVectorB = stitchingVectorRepository.save(privateStitchingVectorB);
	}
	
	@Test
	@WithAnonymousUser
	public void findById_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		// Anonymous user should be able to read a public stitchingVector
		stitchingVectorRepository.findById(publicStitchingVectorA.getId());
		
		// Anonymous user should not be able to read a private stitchingVector
		try {
			stitchingVectorRepository.findById(privateStitchingVectorA.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findById_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		// Non-admin user1 should be able to read own private stitchingVector
		stitchingVectorRepository.findById(privateStitchingVectorA.getId());
				
		// Non-admin user1 should be able to read a public stitchingVector from user2
		stitchingVectorRepository.findById(publicStitchingVectorB.getId());
		
		// Non-admin user1 should not be able to read a private stitchingVector from user2
		try {
			stitchingVectorRepository.findById(privateStitchingVectorB.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findById_adminCallingShouldReturnAllItems() throws Exception {
		
		// Admin should be able to read a public stitchingVector from user1
		stitchingVectorRepository.findById(publicStitchingVectorA.getId());
		
		// Admin should be able to read a private stitchingVector from user1
		stitchingVectorRepository.findById(privateStitchingVectorA.getId());
	}
	
	@Test
	@WithAnonymousUser
	public void findAll_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public stitchingVectors
		Page<StitchingVector> result = stitchingVectorRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(2));
		result.getContent().forEach(stitchingVector -> {
			assertThat(stitchingVector.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findAll_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public stitchingVectors
		Page<StitchingVector> result = stitchingVectorRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(stitchingVector -> {
			assertThat((stitchingVector.isPubliclyShared() || stitchingVector.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findAll_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all stitchingVectors
		Page<StitchingVector> result = stitchingVectorRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(4));
	}
	
	@Test
	@WithAnonymousUser
	public void findByNameContainingIgnoreCase_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public stitchingVectors matching search criteria
		Page<StitchingVector> result = stitchingVectorRepository.findByNameContainingIgnoreCase("stitchingVectorA", pageable);
		assertThat(result.getContent(), hasSize(1));
		result.getContent().forEach(stitchingVector -> {
			assertThat(stitchingVector.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findByNameContainingIgnoreCase_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public stitchingVectors matching search criteria
		Page<StitchingVector> result = stitchingVectorRepository.findByNameContainingIgnoreCase("stitchingVector", pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(stitchingVector -> {
			assertThat((stitchingVector.isPubliclyShared() || stitchingVector.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findByNameContainingIgnoreCase_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all stitchingVectors matching search criteria
		Page<StitchingVector> resultColl = stitchingVectorRepository.findByNameContainingIgnoreCase("stitchingVector", pageable);
		assertThat(resultColl.getContent(), hasSize(4));
		Page<StitchingVector> resultPrivate = stitchingVectorRepository.findByNameContainingIgnoreCase("private", pageable);
		assertThat(resultPrivate.getContent(), hasSize(2));
	}
	
}
