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
package gov.nist.itl.ssd.wipp.backend.data.tensorflowmodels;

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
 * Collection of tests for {@link TensorflowModelRepository} exposed methods
 * Testing access control on READ operations
 * Uses embedded MongoDB database and mock Keycloak users
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class, SecurityConfig.class }, 
				properties = { "spring.data.mongodb.port=0" })
public class TensorflowModelRepositoryTest {
	
	@Autowired WebApplicationContext context;
	@Autowired FilterChainProxy filterChain;

	MockMvc mvc;
	
	@Autowired
	TensorflowModelRepository tensorflowModelRepository;
	
	TensorflowModel publicTensorflowModelA, publicTensorflowModelB, privateTensorflowModelA, privateTensorflowModelB;
	
	@Before
	public void setUp() {
		this.mvc = webAppContextSetup(context)
				.apply(springSecurity())
				.addFilters(filterChain)
				.build();
		
		// Clear embedded database
		tensorflowModelRepository.deleteAll();
		
		// Create and save publicTensorflowModelA (public: true, owner: user1)
		publicTensorflowModelA = new TensorflowModel("publicTensorflowModelA");
		publicTensorflowModelA.setOwner("user1");
		publicTensorflowModelA.setPubliclyShared(true);
		publicTensorflowModelA = tensorflowModelRepository.save(publicTensorflowModelA);
		// Create and save publicTensorflowModelB (public: true, owner: user2)
		publicTensorflowModelB = new TensorflowModel("publicTensorflowModelB");
		publicTensorflowModelB.setOwner("user2");
		publicTensorflowModelB.setPubliclyShared(true);
		publicTensorflowModelB = tensorflowModelRepository.save(publicTensorflowModelB);
		// Create and save privateTensorflowModelA (public: false, owner: user1)
		privateTensorflowModelA = new TensorflowModel("privateTensorflowModelA");
		privateTensorflowModelA.setOwner("user1");
		privateTensorflowModelA.setPubliclyShared(false);
		privateTensorflowModelA = tensorflowModelRepository.save(privateTensorflowModelA);
		// Create and save privateTensorflowModelB (public: false, owner: user2)
		privateTensorflowModelB = new TensorflowModel("privateTensorflowModelB");
		privateTensorflowModelB.setOwner("user2");
		privateTensorflowModelB.setPubliclyShared(false);
		privateTensorflowModelB = tensorflowModelRepository.save(privateTensorflowModelB);
	}
	
	@Test
	@WithAnonymousUser
	public void findById_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		// Anonymous user should be able to read a public tensorflowModel
		tensorflowModelRepository.findById(publicTensorflowModelA.getId());
		
		// Anonymous user should not be able to read a private tensorflowModel
		try {
			tensorflowModelRepository.findById(privateTensorflowModelA.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findById_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		// Non-admin user1 should be able to read own private tensorflowModel
		tensorflowModelRepository.findById(privateTensorflowModelA.getId());
				
		// Non-admin user1 should be able to read a public tensorflowModel from user2
		tensorflowModelRepository.findById(publicTensorflowModelB.getId());
		
		// Non-admin user1 should not be able to read a private tensorflowModel from user2
		try {
			tensorflowModelRepository.findById(privateTensorflowModelB.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findById_adminCallingShouldReturnAllItems() throws Exception {
		
		// Admin should be able to read a public tensorflowModel from user1
		tensorflowModelRepository.findById(publicTensorflowModelA.getId());
		
		// Admin should be able to read a private tensorflowModel from user1
		tensorflowModelRepository.findById(privateTensorflowModelA.getId());
	}
	
	@Test
	@WithAnonymousUser
	public void findAll_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public tensorflowModels
		Page<TensorflowModel> result = tensorflowModelRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(2));
		result.getContent().forEach(tensorflowModel -> {
			assertThat(tensorflowModel.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findAll_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public tensorflowModels
		Page<TensorflowModel> result = tensorflowModelRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(tensorflowModel -> {
			assertThat((tensorflowModel.isPubliclyShared() || tensorflowModel.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findAll_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all tensorflowModels
		Page<TensorflowModel> result = tensorflowModelRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(4));
	}
	
	@Test
	@WithAnonymousUser
	public void findByNameContainingIgnoreCase_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public tensorflowModels matching search criteria
		Page<TensorflowModel> result = tensorflowModelRepository.findByNameContainingIgnoreCase("tensorflowModelA", pageable);
		assertThat(result.getContent(), hasSize(1));
		result.getContent().forEach(tensorflowModel -> {
			assertThat(tensorflowModel.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findByNameContainingIgnoreCase_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public tensorflowModels matching search criteria
		Page<TensorflowModel> result = tensorflowModelRepository.findByNameContainingIgnoreCase("tensorflowModel", pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(tensorflowModel -> {
			assertThat((tensorflowModel.isPubliclyShared() || tensorflowModel.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findByNameContainingIgnoreCase_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all tensorflowModels matching search criteria
		Page<TensorflowModel> resultColl = tensorflowModelRepository.findByNameContainingIgnoreCase("tensorflowModel", pageable);
		assertThat(resultColl.getContent(), hasSize(4));
		Page<TensorflowModel> resultPrivate = tensorflowModelRepository.findByNameContainingIgnoreCase("private", pageable);
		assertThat(resultPrivate.getContent(), hasSize(2));
	}
	
}
