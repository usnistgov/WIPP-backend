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
package gov.nist.itl.ssd.wipp.backend.data.tensorboard;

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
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobRepository;
//import gov.nist.itl.ssd.wipp.backend.securityutils.WithMockKeycloakUser;

/**
 * Collection of tests for {@link TensorboardLogsRepository} exposed methods
 * Testing access control on READ operations
 * Uses embedded MongoDB database and mock Keycloak users
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { Application.class, SecurityConfig.class }, 
				properties = { "spring.data.mongodb.port=0" })
public class TensorboardLogsRepositoryTest {
	
	@Autowired WebApplicationContext context;
	@Autowired FilterChainProxy filterChain;

	MockMvc mvc;
	
	@Autowired
	TensorboardLogsRepository tensorboardLogsRepository;
	
	@Autowired
	JobRepository jobRepository;
	
	TensorboardLogs publicTensorboardLogsA, publicTensorboardLogsB, privateTensorboardLogsA, privateTensorboardLogsB;
	Job publicJobA, publicJobB, privateJobA, privateJobB;

	
	@BeforeAll
	public void setUp() {
		this.mvc = webAppContextSetup(context)
				.apply(springSecurity())
				.addFilters(filterChain)
				.build();
		
		// Clear embedded database
		tensorboardLogsRepository.deleteAll();
		jobRepository.deleteAll();

		// Create and save publicTensorboardLogsA (public: true, owner: user1, job: publicJobA)
		publicJobA = new Job();
		publicJobA.setName("publicJobA");
		publicJobA = (Job) jobRepository.save(publicJobA);
		publicTensorboardLogsA = new TensorboardLogs(publicJobA, "tensorboardLogs");
		publicTensorboardLogsA.setOwner("user1");
		publicTensorboardLogsA.setPubliclyShared(true);
		publicTensorboardLogsA = tensorboardLogsRepository.save(publicTensorboardLogsA);
		// Create and save publicTensorboardLogsB (public: true, owner: user2, job: publicJobB)
		publicJobB = new Job();
		publicJobB.setName("publicJobB");
		publicJobB = (Job) jobRepository.save(publicJobB);
		publicTensorboardLogsB = new TensorboardLogs(publicJobB, "tensorboardLogs");
		publicTensorboardLogsB.setOwner("user2");
		publicTensorboardLogsB.setPubliclyShared(true);
		publicTensorboardLogsB = tensorboardLogsRepository.save(publicTensorboardLogsB);
		// Create and save privateTensorboardLogsA (public: false, owner: user1, job: privateJobA)
		privateJobA = new Job();
		privateJobA.setName("privateJobA");
		privateJobA = (Job) jobRepository.save(privateJobA);
		privateTensorboardLogsA = new TensorboardLogs(privateJobA, "tensorboardLogs");
		privateTensorboardLogsA.setOwner("user1");
		privateTensorboardLogsA.setPubliclyShared(false);
		privateTensorboardLogsA = tensorboardLogsRepository.save(privateTensorboardLogsA);
		// Create and save privateTensorboardLogsB (public: false, owner: user2, , job: privateJobB)
		privateJobB = new Job();
		privateJobB.setName("privateJobB");
		privateJobB = (Job) jobRepository.save(privateJobB);
		privateTensorboardLogsB = new TensorboardLogs(privateJobB, "tensorboardLogs");
		privateTensorboardLogsB.setOwner("user2");
		privateTensorboardLogsB.setPubliclyShared(false);
		privateTensorboardLogsB = tensorboardLogsRepository.save(privateTensorboardLogsB);
	}
	
	@Test
	@WithAnonymousUser
	public void findById_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		// Anonymous user should be able to read a public tensorboardLogs
		tensorboardLogsRepository.findById(publicTensorboardLogsA.getId());
		
		// Anonymous user should not be able to read a private tensorboardLogs
		try {
			tensorboardLogsRepository.findById(privateTensorboardLogsA.getId());
			Assertions.fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
	
	@Test
	@WithMockUser(username="user1", roles={ "user" })
	public void findById_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		// Non-admin user1 should be able to read own private tensorboardLogs
		tensorboardLogsRepository.findById(privateTensorboardLogsA.getId());
				
		// Non-admin user1 should be able to read a public tensorboardLogs from user2
		tensorboardLogsRepository.findById(publicTensorboardLogsB.getId());
		
		// Non-admin user1 should not be able to read a private tensorboardLogs from user2
		try {
			tensorboardLogsRepository.findById(privateTensorboardLogsB.getId());
			Assertions.fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	@WithMockUser(username="admin", roles={ "admin" })
	public void findById_adminCallingShouldReturnAllItems() throws Exception {
		
		// Admin should be able to read a public tensorboardLogs from user1
		tensorboardLogsRepository.findById(publicTensorboardLogsA.getId());
		
		// Admin should be able to read a private tensorboardLogs from user1
		tensorboardLogsRepository.findById(privateTensorboardLogsA.getId());
	}
	
	@Test
	@WithAnonymousUser
	public void findAll_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public tensorboardLogs
		Page<TensorboardLogs> result = tensorboardLogsRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(2));
		result.getContent().forEach(tensorboardLogs -> {
			assertThat(tensorboardLogs.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockUser(username="user1", roles={ "user" })
	public void findAll_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public tensorboardLogs
		Page<TensorboardLogs> result = tensorboardLogsRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(tensorboardLogs -> {
			assertThat((tensorboardLogs.isPubliclyShared() || tensorboardLogs.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockUser(username="admin", roles={ "admin" })
	public void findAll_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all tensorboardLogs
		Page<TensorboardLogs> result = tensorboardLogsRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(4));
	}
	
	@Test
	@WithAnonymousUser
	public void findByNameContainingIgnoreCase_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public tensorboardLogs matching search criteria
		Page<TensorboardLogs> result = tensorboardLogsRepository.findByNameContainingIgnoreCase("a-tensorboardLogs", pageable);
		assertThat(result.getContent(), hasSize(1));
		result.getContent().forEach(tensorboardLogs -> {
			assertThat(tensorboardLogs.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockUser(username="user1", roles={ "user" })
	public void findByNameContainingIgnoreCase_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public tensorboardLogs matching search criteria
		Page<TensorboardLogs> result = tensorboardLogsRepository.findByNameContainingIgnoreCase("tensorboardLogs", pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(tensorboardLogs -> {
			assertThat((tensorboardLogs.isPubliclyShared() || tensorboardLogs.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockUser(username="admin", roles={ "admin" })
	public void findByNameContainingIgnoreCase_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all tensorboardLogs matching search criteria
		Page<TensorboardLogs> resultColl = tensorboardLogsRepository.findByNameContainingIgnoreCase("tensorboardLogs", pageable);
		assertThat(resultColl.getContent(), hasSize(4));
		Page<TensorboardLogs> resultPrivate = tensorboardLogsRepository.findByNameContainingIgnoreCase("private", pageable);
		assertThat(resultPrivate.getContent(), hasSize(2));
	}
	
	@Test
	@WithAnonymousUser
	public void findOneBySourceJob_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		// Anonymous user should be able to read a public tensorboardLogs matching the search criteria
		tensorboardLogsRepository.findOneBySourceJob(publicJobA.getId());
		
		// Anonymous user should not be able to read a private tensorboardLogs matching the search criteria
		try {
			tensorboardLogsRepository.findOneBySourceJob(privateJobA.getId());
			Assertions.fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
	
	@Test
	@WithMockUser(username="user1", roles={ "user" })
	public void findOneBySourceJob_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		// Non-admin user1 should be able to read own private tensorboardLogs matching the search criteria
		tensorboardLogsRepository.findOneBySourceJob(privateJobA.getId());
				
		// Non-admin user1 should be able to read a public tensorboardLogs from user2 matching the search criteria
		tensorboardLogsRepository.findOneBySourceJob(publicJobB.getId());
		
		// Non-admin user1 should not be able to read a private tensorboardLogs from user2 matching the search criteria
		try {
			tensorboardLogsRepository.findOneBySourceJob(privateJobB.getId());
			Assertions.fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	@WithMockUser(username="admin", roles={ "admin" })
	public void findOneBySourceJob_adminCallingShouldReturnAllItems() throws Exception {
		
		// Admin should be able to read a public tensorboardLogs from user1 matching the search criteria
		tensorboardLogsRepository.findOneBySourceJob(publicJobA.getId());
		
		// Admin should be able to read a private tensorboardLogs from user1 matching the search criteria
		tensorboardLogsRepository.findOneBySourceJob(privateJobA.getId());
	}
	
}
