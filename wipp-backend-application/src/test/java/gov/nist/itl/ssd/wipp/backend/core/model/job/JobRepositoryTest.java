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
package gov.nist.itl.ssd.wipp.backend.core.model.job;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.util.List;

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
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.Workflow;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowRepository;
import gov.nist.itl.ssd.wipp.backend.securityutils.WithMockKeycloakUser;

/**
 * Collection of tests for {@link JobRepository} exposed methods
 * Testing access control on READ operations
 * Uses embedded MongoDB database and mock Keycloak users
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class, SecurityConfig.class }, 
				properties = { "spring.data.mongodb.port=0" })
public class JobRepositoryTest {
	
	@Autowired WebApplicationContext context;
	@Autowired FilterChainProxy filterChain;

	MockMvc mvc;
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	WorkflowRepository workflowRepository;
	
	Job publicJobA, publicJobB, privateJobA, privateJobB;
	Workflow publicWorkflowA, publicWorkflowB, privateWorkflowA, privateWorkflowB;
	
	@Before
	public void setUp() {
		this.mvc = webAppContextSetup(context)
				.apply(springSecurity())
				.addFilters(filterChain)
				.build();
		
		// Clear embedded database
		jobRepository.deleteAll();
		
		// Create and save publicJobA (public: true, owner: user1)
		publicWorkflowA = new Workflow("publicWorkflowA");
		publicWorkflowA.setOwner("user1");
		publicWorkflowA.setPubliclyShared(true);
		publicWorkflowA = workflowRepository.save(publicWorkflowA);
		publicJobA = new Job();
		publicJobA.setName("publicJobA");
		publicJobA.setOwner("user1");
		publicJobA.setWippWorkflow(publicWorkflowA.getId());
		publicJobA.setStatus(JobStatus.CREATED);
		publicJobA.setPubliclyShared(true);
		publicJobA = (Job) jobRepository.save(publicJobA);
		// Create and save publicJobB (public: true, owner: user2)
		publicWorkflowB = new Workflow("publicWorkflowB");
		publicWorkflowB.setOwner("user2");
		publicWorkflowB.setPubliclyShared(true);
		publicWorkflowB = workflowRepository.save(publicWorkflowB);
		publicJobB = new Job();
		publicJobB.setName("publicJobB");
		publicJobB.setOwner("user2");
		publicJobB.setWippWorkflow(publicWorkflowB.getId());
		publicJobB.setStatus(JobStatus.CREATED);
		publicJobB.setPubliclyShared(true);
		publicJobB = (Job) jobRepository.save(publicJobB);
		// Create and save privateJobA (public: false, owner: user1)
		privateWorkflowA = new Workflow("privateWorkflowA");
		privateWorkflowA.setOwner("user1");
		privateWorkflowA.setPubliclyShared(false);
		privateWorkflowA = workflowRepository.save(privateWorkflowA);
		privateJobA = new Job();
		privateJobA.setName("privateJobA");
		privateJobA.setOwner("user1");
		privateJobA.setWippWorkflow(privateWorkflowA.getId());
		privateJobA.setStatus(JobStatus.CREATED);
		privateJobA.setPubliclyShared(false);
		privateJobA = (Job) jobRepository.save(privateJobA);
		// Create and save privateJobB (public: false, owner: user2)
		privateWorkflowB = new Workflow("privateWorkflowB");
		privateWorkflowB.setOwner("user2");
		privateWorkflowB.setPubliclyShared(false);
		privateWorkflowB = workflowRepository.save(privateWorkflowB);
		privateJobB = new Job();
		privateJobB.setName("privateJobB");
		privateJobB.setOwner("user2");
		privateJobB.setWippWorkflow(privateWorkflowB.getId());
		privateJobB.setStatus(JobStatus.CREATED);
		privateJobB.setPubliclyShared(false);
		privateJobB = (Job) jobRepository.save(privateJobB);
	}
	
	@Test
	@WithAnonymousUser
	public void findById_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		// Anonymous user should be able to read a public job
		jobRepository.findById(publicJobA.getId());
		
		// Anonymous user should not be able to read a private job
		try {
			jobRepository.findById(privateJobA.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findById_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		// Non-admin user1 should be able to read own private job
		jobRepository.findById(privateJobA.getId());
				
		// Non-admin user1 should be able to read a public job from user2
		jobRepository.findById(publicJobB.getId());
		
		// Non-admin user1 should not be able to read a private job from user2
		try {
			jobRepository.findById(privateJobB.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findById_adminCallingShouldReturnAllItems() throws Exception {
		
		// Admin should be able to read a public job from user1
		jobRepository.findById(publicJobA.getId());
		
		// Admin should be able to read a private job from user1
		jobRepository.findById(privateJobA.getId());
	}
	
	@Test
	@WithAnonymousUser
	public void findAll_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public jobs
		Page<Job> result = jobRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(2));
		result.getContent().forEach(job -> {
			assertThat(job.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findAll_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public jobs
		Page<Job> result = jobRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(job -> {
			assertThat((job.isPubliclyShared() || job.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findAll_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all jobs
		Page<Job> result = jobRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(4));
	}
	
	@Test
	@WithAnonymousUser
	public void findByNameContainingIgnoreCase_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public jobs matching search criteria
		Page<Job> result = jobRepository.findByNameContainingIgnoreCase("jobA", pageable);
		assertThat(result.getContent(), hasSize(1));
		result.getContent().forEach(job -> {
			assertThat(job.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findByNameContainingIgnoreCase_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public jobs matching search criteria
		Page<Job> result = jobRepository.findByNameContainingIgnoreCase("job", pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(job -> {
			assertThat((job.isPubliclyShared() || job.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findByNameContainingIgnoreCase_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all jobs matching search criteria
		Page<Job> resultColl = jobRepository.findByNameContainingIgnoreCase("job", pageable);
		assertThat(resultColl.getContent(), hasSize(4));
		Page<Job> resultPrivate = jobRepository.findByNameContainingIgnoreCase("private", pageable);
		assertThat(resultPrivate.getContent(), hasSize(2));
	}
	
	@Test
	@WithAnonymousUser
	public void findByStatus_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public jobs matching search criteria
		Page<Job> resultCreated = jobRepository.findByStatus(JobStatus.CREATED, pageable);
		assertThat(resultCreated.getContent(), hasSize(2));
		resultCreated.getContent().forEach(job -> {
			assertThat(job.isPubliclyShared(), is(true));
		});
		Page<Job> resultSucceeded = jobRepository.findByStatus(JobStatus.SUCCEEDED, pageable);
		assertThat(resultSucceeded.getContent(), hasSize(0));
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findByStatus_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public jobs matching search criteria
		Page<Job> resultCreated = jobRepository.findByStatus(JobStatus.CREATED, pageable);
		assertThat(resultCreated.getContent(), hasSize(3));
		resultCreated.getContent().forEach(job -> {
			assertThat((job.isPubliclyShared() || job.getOwner().equals("user1")), is(true));
		});
		Page<Job> resultSucceeded = jobRepository.findByStatus(JobStatus.SUCCEEDED, pageable);
		assertThat(resultSucceeded.getContent(), hasSize(0));
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findByStatus_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all jobs matching search criteria
		Page<Job> resultColl = jobRepository.findByStatus(JobStatus.CREATED, pageable);
		assertThat(resultColl.getContent(), hasSize(4));
		Page<Job> resultSucceeded = jobRepository.findByStatus(JobStatus.SUCCEEDED, pageable);
		assertThat(resultSucceeded.getContent(), hasSize(0));
	}
	
	@Test
	@WithAnonymousUser
	public void findByNameContainingIgnoreCaseAndStatus_anonymousCallingShouldReturnOnlyPublicItems() 
			throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public jobs matching search criteria
		Page<Job> resultCreated = jobRepository.findByNameContainingIgnoreCaseAndStatus(
				"jobA", JobStatus.CREATED.name(), pageable);
		assertThat(resultCreated.getContent(), hasSize(1));
		resultCreated.getContent().forEach(job -> {
			assertThat(job.isPubliclyShared(), is(true));
		});
		Page<Job> resultSucceeded = jobRepository.findByNameContainingIgnoreCaseAndStatus(
				"jobA", JobStatus.SUCCEEDED.name(), pageable);
		assertThat(resultSucceeded.getContent(), hasSize(0));
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findByNameContainingIgnoreCaseAndStatus_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() 
			throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public jobs matching search criteria
		Page<Job> resultCreated = jobRepository.findByNameContainingIgnoreCaseAndStatus(
				"job", JobStatus.CREATED.name(), pageable);
		assertThat(resultCreated.getContent(), hasSize(3));
		resultCreated.getContent().forEach(job -> {
			assertThat((job.isPubliclyShared() || job.getOwner().equals("user1")), is(true));
		});
		Page<Job> resultSucceeded = jobRepository.findByNameContainingIgnoreCaseAndStatus(
				"job", JobStatus.SUCCEEDED.name(), pageable);
		assertThat(resultSucceeded.getContent(), hasSize(0));
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findByNameContainingIgnoreCaseAndStatus_adminCallingShouldReturnAllItems() 
			throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all jobs matching search criteria
		Page<Job> resultColl = jobRepository.findByNameContainingIgnoreCaseAndStatus(
				"job", JobStatus.CREATED.name(), pageable);
		assertThat(resultColl.getContent(), hasSize(4));
		Page<Job> resultSucceeded = jobRepository.findByNameContainingIgnoreCaseAndStatus(
				"job", JobStatus.SUCCEEDED.name(), pageable);
		assertThat(resultSucceeded.getContent(), hasSize(0));
	}
	
	@Test
	@WithAnonymousUser
	public void findByWippWorkflow_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public jobs matching search criteria
		Page<Job> resultCreated = jobRepository.findByWippWorkflow(publicWorkflowA.getId(), pageable);
		assertThat(resultCreated.getContent(), hasSize(1));
		resultCreated.getContent().forEach(job -> {
			assertThat(job.isPubliclyShared(), is(true));
		});
		try {
			jobRepository.findByWippWorkflow(privateWorkflowA.getId(), pageable);
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findByWippWorkflow_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public jobs matching search criteria
		Page<Job> resultPrivateA = jobRepository.findByWippWorkflow(privateWorkflowA.getId(), pageable);
		assertThat(resultPrivateA.getContent(), hasSize(1));
		resultPrivateA.getContent().forEach(job -> {
			assertThat((job.getOwner().equals("user1")), is(true));
		});
		Page<Job> resultPublicB = jobRepository.findByWippWorkflow(publicWorkflowB.getId(), pageable);
		assertThat(resultPublicB.getContent(), hasSize(1));
		resultPublicB.getContent().forEach(job -> {
			assertThat(job.isPubliclyShared(), is(true));
		});
		try {
			jobRepository.findByWippWorkflow(privateWorkflowB.getId(), pageable);
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findByWippWorkflow_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all jobs matching search criteria
		Page<Job> resultPublic = jobRepository.findByWippWorkflow(publicWorkflowA.getId(), pageable);
		assertThat(resultPublic.getContent(), hasSize(1));
		Page<Job> resultPrivate = jobRepository.findByWippWorkflow(privateWorkflowA.getId(), pageable);
		assertThat(resultPrivate.getContent(), hasSize(1));
	}
	
	@Test
	@WithAnonymousUser
	public void findByWippWorkflowOrderByCreationDateAsc_anonymousCallingShouldReturnOnlyPublicItems() 
			throws Exception {
		
		// Anonymous user should get only get list of public jobs matching search criteria
		List<Job> resultCreated = jobRepository.findByWippWorkflowOrderByCreationDateAsc(publicWorkflowA.getId());
		assertThat(resultCreated, hasSize(1));
		resultCreated.forEach(job -> {
			assertThat(job.isPubliclyShared(), is(true));
		});
		try {
			jobRepository.findByWippWorkflowOrderByCreationDateAsc(privateWorkflowA.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findByWippWorkflowOrderByCreationDateAsc_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() 
			throws Exception {
		
		// Non-admin user1 should only get list of own and public jobs matching search criteria
		List<Job> resultPrivateA = jobRepository.findByWippWorkflowOrderByCreationDateAsc(privateWorkflowA.getId());
		assertThat(resultPrivateA, hasSize(1));
		resultPrivateA.forEach(job -> {
			assertThat((job.getOwner().equals("user1")), is(true));
		});
		List<Job> resultPublicB = jobRepository.findByWippWorkflowOrderByCreationDateAsc(publicWorkflowB.getId());
		assertThat(resultPublicB, hasSize(1));
		resultPublicB.forEach(job -> {
			assertThat(job.isPubliclyShared(), is(true));
		});
		try {
			jobRepository.findByWippWorkflowOrderByCreationDateAsc(privateWorkflowB.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findByWippWorkflowOrderByCreationDateAsc_adminCallingShouldReturnAllItems() 
			throws Exception {
		
		// Admin should get list of all jobs matching search criteria
		List<Job> resultPublic = jobRepository.findByWippWorkflowOrderByCreationDateAsc(publicWorkflowA.getId());
		assertThat(resultPublic, hasSize(1));
		List<Job> resultPrivate = jobRepository.findByWippWorkflow(privateWorkflowA.getId());
		assertThat(resultPrivate, hasSize(1));
	}
}
