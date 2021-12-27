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
package gov.nist.itl.ssd.wipp.backend.data.genericdatacollection;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

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
import gov.nist.itl.ssd.wipp.backend.data.genericdatacollection.GenericDataCollection;
import gov.nist.itl.ssd.wipp.backend.data.genericdatacollection.GenericDataCollectionRepository;
import gov.nist.itl.ssd.wipp.backend.securityutils.WithMockKeycloakUser;

/**
 * Collection of tests for {@link GenericDataCollectionRepository} exposed methods
 * Testing access control on READ operations
 * Uses embedded MongoDB database and mock Keycloak users
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class, SecurityConfig.class }, 
				properties = { "spring.data.mongodb.port=0" })
public class GenericDataCollectionRepositoryTest {

	@Autowired WebApplicationContext context;
	@Autowired FilterChainProxy filterChain;

	MockMvc mvc;
	
	@Autowired
	GenericDataCollectionRepository genericDataCollectionRepository;
	
	GenericDataCollection publicGenDataA, publicGenDataB, privateGenDataA, privateGenDataB;
	
	@Before
	public void setUp() {
		this.mvc = webAppContextSetup(context)
				.apply(springSecurity())
				.addFilters(filterChain)
				.build();
		
		// Clear embedded database
		genericDataCollectionRepository.deleteAll();
		
		// Create and save publicGenDataA (public: true, owner: user1)
		publicGenDataA = new GenericDataCollection("publicGenDataA", false);
		publicGenDataA.setOwner("user1");
		publicGenDataA.setPubliclyShared(true);
		publicGenDataA = genericDataCollectionRepository.save(publicGenDataA);
		// Create and save publicGenDataB (public: true, owner: user2)
		publicGenDataB = new GenericDataCollection("publicGenDataB", false);
		publicGenDataB.setOwner("user2");
		publicGenDataB.setPubliclyShared(true);
		publicGenDataB = genericDataCollectionRepository.save(publicGenDataB);
		// Create and save privateGenDataA (public: false, owner: user1)
		privateGenDataA = new GenericDataCollection("privateGenDataA", false);
		privateGenDataA.setOwner("user1");
		privateGenDataA.setPubliclyShared(false);
		privateGenDataA = genericDataCollectionRepository.save(privateGenDataA);
		// Create and save privateGenDataB (public: false, owner: user2)
		privateGenDataB = new GenericDataCollection("privateGenDataB", false);
		privateGenDataB.setOwner("user2");
		privateGenDataB.setPubliclyShared(false);
		privateGenDataB = genericDataCollectionRepository.save(privateGenDataB);
	}
	
	@Test
	@WithAnonymousUser
	public void findById_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		// Anonymous user should be able to read a public collection
		genericDataCollectionRepository.findById(publicGenDataA.getId());
		
		// Anonymous user should not be able to read a private collection
		try {
			genericDataCollectionRepository.findById(privateGenDataA.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findById_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		// Non-admin user1 should be able to read own private collection
		genericDataCollectionRepository.findById(privateGenDataA.getId());
				
		// Non-admin user1 should be able to read a public collection from user2
		genericDataCollectionRepository.findById(publicGenDataB.getId());
		
		// Non-admin user1 should not be able to read a private collection from user2
		try {
			genericDataCollectionRepository.findById(privateGenDataB.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findById_adminCallingShouldReturnAllItems() throws Exception {
		
		// Admin should be able to read a public collection from user1
		genericDataCollectionRepository.findById(publicGenDataA.getId());
		
		// Admin should be able to read a private collection from user1
		genericDataCollectionRepository.findById(privateGenDataA.getId());
	}
	
	@Test
	@WithAnonymousUser
	public void findAll_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public collections
		Page<GenericDataCollection> result = genericDataCollectionRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(2));
		result.getContent().forEach(csvGenData -> {
			assertThat(csvGenData.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findAll_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public collections
		Page<GenericDataCollection> result = genericDataCollectionRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(csvGenData -> {
			assertThat((csvGenData.isPubliclyShared() || csvGenData.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findAll_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all collections
		Page<GenericDataCollection> result = genericDataCollectionRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(4));
	}
	
	@Test
	@WithAnonymousUser
	public void findByNameContainingIgnoreCase_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public collections matching search criteria
		Page<GenericDataCollection> result = genericDataCollectionRepository.findByNameContainingIgnoreCase("gendataA", pageable);
		assertThat(result.getContent(), hasSize(1));
		result.getContent().forEach(csvGenData -> {
			assertThat(csvGenData.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findByNameContainingIgnoreCase_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public collections matching search criteria
		Page<GenericDataCollection> result = genericDataCollectionRepository.findByNameContainingIgnoreCase("gendata", pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(csvGenData -> {
			assertThat((csvGenData.isPubliclyShared() || csvGenData.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findByNameContainingIgnoreCase_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all collections matching search criteria
		Page<GenericDataCollection> resultGenData = genericDataCollectionRepository.findByNameContainingIgnoreCase("gendata", pageable);
		assertThat(resultGenData.getContent(), hasSize(4));
		Page<GenericDataCollection> resultPrivate = genericDataCollectionRepository.findByNameContainingIgnoreCase("private", pageable);
		assertThat(resultPrivate.getContent(), hasSize(2));
	}
	
}
