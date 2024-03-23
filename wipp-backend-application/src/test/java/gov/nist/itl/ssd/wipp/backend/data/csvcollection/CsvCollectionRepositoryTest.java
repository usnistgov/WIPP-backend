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
package gov.nist.itl.ssd.wipp.backend.data.csvcollection;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import gov.nist.itl.ssd.wipp.backend.Application;
import gov.nist.itl.ssd.wipp.backend.app.SecurityConfig;
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.CsvCollection;
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.CsvCollectionRepository;
//import gov.nist.itl.ssd.wipp.backend.securityutils.WithMockKeycloakUser;

/**
 * Collection of tests for {@link CsvCollectionRepository} exposed methods
 * Testing access control on READ operations
 * Uses embedded MongoDB database and mock Keycloak users
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { Application.class, SecurityConfig.class }, 
				properties = { "spring.data.mongodb.port=0" })
public class CsvCollectionRepositoryTest {
	
	@Autowired WebApplicationContext context;
	@Autowired FilterChainProxy filterChain;

	MockMvc mvc;
	
	@Autowired
	CsvCollectionRepository csvCollectionRepository;
	
	CsvCollection publicCollA, publicCollB, privateCollA, privateCollB;
	
	@BeforeAll
	public void setUp() {
		this.mvc = webAppContextSetup(context)
				.apply(springSecurity())
				.addFilters(filterChain)
				.build();
		
		// Clear embedded database
		csvCollectionRepository.deleteAll();
		
		// Create and save publicCollA (public: true, owner: user1)
		publicCollA = new CsvCollection("publicCollA", false);
		publicCollA.setOwner("user1");
		publicCollA.setPubliclyShared(true);
		publicCollA = csvCollectionRepository.save(publicCollA);
		// Create and save publicCollB (public: true, owner: user2)
		publicCollB = new CsvCollection("publicCollB", false);
		publicCollB.setOwner("user2");
		publicCollB.setPubliclyShared(true);
		publicCollB = csvCollectionRepository.save(publicCollB);
		// Create and save privateCollA (public: false, owner: user1)
		privateCollA = new CsvCollection("privateCollA", false);
		privateCollA.setOwner("user1");
		privateCollA.setPubliclyShared(false);
		privateCollA = csvCollectionRepository.save(privateCollA);
		// Create and save privateCollB (public: false, owner: user2)
		privateCollB = new CsvCollection("privateCollB", false);
		privateCollB.setOwner("user2");
		privateCollB.setPubliclyShared(false);
		privateCollB = csvCollectionRepository.save(privateCollB);
	}
	
	@Test
	@WithAnonymousUser
	public void findById_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		// Anonymous user should be able to read a public collection
		csvCollectionRepository.findById(publicCollA.getId());
		
		// Anonymous user should not be able to read a private collection
		try {
			csvCollectionRepository.findById(privateCollA.getId());
			Assertions.fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
	
	@Test
	@WithMockUser(username="user1", roles={ "user" })
	public void findById_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		// Non-admin user1 should be able to read own private collection
		csvCollectionRepository.findById(privateCollA.getId());
				
		// Non-admin user1 should be able to read a public collection from user2
		csvCollectionRepository.findById(publicCollB.getId());
		
		// Non-admin user1 should not be able to read a private collection from user2
		try {
			csvCollectionRepository.findById(privateCollB.getId());
			Assertions.fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	@WithMockUser(username="admin", roles={ "admin" })
	public void findById_adminCallingShouldReturnAllItems() throws Exception {
		
		// Admin should be able to read a public collection from user1
		csvCollectionRepository.findById(publicCollA.getId());
		
		// Admin should be able to read a private collection from user1
		csvCollectionRepository.findById(privateCollA.getId());
	}
	
	@Test
	@WithAnonymousUser
	public void findAll_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public collections
		Page<CsvCollection> result = csvCollectionRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(2));
		result.getContent().forEach(csvColl -> {
			assertThat(csvColl.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockUser(username="user1", roles={ "user" })
	public void findAll_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public collections
		Page<CsvCollection> result = csvCollectionRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(csvColl -> {
			assertThat((csvColl.isPubliclyShared() || csvColl.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockUser(username="admin", roles={ "admin" })
	public void findAll_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all collections
		Page<CsvCollection> result = csvCollectionRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(4));
	}
	
	@Test
	@WithAnonymousUser
	public void findByNameContainingIgnoreCase_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public collections matching search criteria
		Page<CsvCollection> result = csvCollectionRepository.findByNameContainingIgnoreCase("collA", pageable);
		assertThat(result.getContent(), hasSize(1));
		result.getContent().forEach(csvColl -> {
			assertThat(csvColl.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockUser(username="user1", roles={ "user" })
	public void findByNameContainingIgnoreCase_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public collections matching search criteria
		Page<CsvCollection> result = csvCollectionRepository.findByNameContainingIgnoreCase("coll", pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(csvColl -> {
			assertThat((csvColl.isPubliclyShared() || csvColl.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockUser(username="admin", roles={ "admin" })
	public void findByNameContainingIgnoreCase_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all collections matching search criteria
		Page<CsvCollection> resultColl = csvCollectionRepository.findByNameContainingIgnoreCase("coll", pageable);
		assertThat(resultColl.getContent(), hasSize(4));
		Page<CsvCollection> resultPrivate = csvCollectionRepository.findByNameContainingIgnoreCase("private", pageable);
		assertThat(resultPrivate.getContent(), hasSize(2));
	}
	
	@Test
	@WithAnonymousUser
	public void findByName_anonymousCallingShouldReturnOnlyPublicItems() 
			throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public collections matching search criteria
		Page<CsvCollection> resultPublic = csvCollectionRepository
				.findByName("publicCollA", pageable);
		assertThat(resultPublic.getContent(), hasSize(1));
		resultPublic.getContent().forEach(csvColl -> {
			assertThat(csvColl.isPubliclyShared(), is(true));
		});
		Page<CsvCollection> resultPrivate = csvCollectionRepository
				.findByName("privateCollA", pageable);
		assertThat(resultPrivate.getContent(), hasSize(0));
	}
	
	@Test
	@WithMockUser(username="user1", roles={ "user" })
	public void findByName_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() 
			throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public collections matching search criteria
		Page<CsvCollection> resultOwnPrivate = csvCollectionRepository
				.findByName("privateCollA", pageable);
		assertThat(resultOwnPrivate.getContent(), hasSize(1));
		resultOwnPrivate.getContent().forEach(csvColl -> {
			assertThat((csvColl.isPubliclyShared() || csvColl.getOwner().equals("user1")), is(true));
		});
		Page<CsvCollection> resultOtherPrivate = csvCollectionRepository
				.findByName("privateCollB", pageable);
		assertThat(resultOtherPrivate.getContent(), hasSize(0));
	}

	@Test
	@WithMockUser(username="admin", roles={ "admin" })
	public void findByName_adminCallingShouldReturnAllItems() 
			throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all collections matching search criteria
		Page<CsvCollection> resultColl = csvCollectionRepository
				.findByName("publicCollA", pageable);
		assertThat(resultColl.getContent(), hasSize(1));
		Page<CsvCollection> resultPrivate = csvCollectionRepository
				.findByName("privateCollA", pageable);
		assertThat(resultPrivate.getContent(), hasSize(1));
	}

}
