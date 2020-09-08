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
package gov.nist.itl.ssd.wipp.backend.data.jupyternotebook;

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
 * Collection of tests for {@link NotebookRepository} exposed methods
 * Testing access control on READ operations
 * Uses embedded MongoDB database and mock Keycloak users
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class, SecurityConfig.class }, 
				properties = { "spring.data.mongodb.port=0" })
public class NotebookRepositoryTest {
	
	@Autowired WebApplicationContext context;
	@Autowired FilterChainProxy filterChain;

	MockMvc mvc;
	
	@Autowired
	NotebookRepository notebookRepository;
	
	Notebook publicNotebookA, publicNotebookB, privateNotebookA, privateNotebookB;
	
	@Before
	public void setUp() {
		this.mvc = webAppContextSetup(context)
				.apply(springSecurity())
				.addFilters(filterChain)
				.build();
		
		// Clear embedded database
		notebookRepository.deleteAll();
		
		// Create and save publicNotebookA (public: true, owner: user1)
		publicNotebookA = new Notebook("publicNotebookA", "publicNotebookA");
		publicNotebookA.setOwner("user1");
		publicNotebookA.setPubliclyShared(true);
		publicNotebookA = notebookRepository.save(publicNotebookA);
		// Create and save publicNotebookB (public: true, owner: user2)
		publicNotebookB = new Notebook("publicNotebookB", "publicNotebookB");
		publicNotebookB.setOwner("user2");
		publicNotebookB.setPubliclyShared(true);
		publicNotebookB = notebookRepository.save(publicNotebookB);
		// Create and save privateNotebookA (public: false, owner: user1)
		privateNotebookA = new Notebook("privateNotebookA", "privateNotebookA");
		privateNotebookA.setOwner("user1");
		privateNotebookA.setPubliclyShared(false);
		privateNotebookA = notebookRepository.save(privateNotebookA);
		// Create and save privateNotebookB (public: false, owner: user2)
		privateNotebookB = new Notebook("privateNotebookB", "privateNotebookB");
		privateNotebookB.setOwner("user2");
		privateNotebookB.setPubliclyShared(false);
		privateNotebookB = notebookRepository.save(privateNotebookB);
	}
	
	@Test
	@WithAnonymousUser
	public void findById_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		// Anonymous user should be able to read a public notebook
		notebookRepository.findById(publicNotebookA.getId());
		
		// Anonymous user should not be able to read a private notebook
		try {
			notebookRepository.findById(privateNotebookA.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findById_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		// Non-admin user1 should be able to read own private notebook
		notebookRepository.findById(privateNotebookA.getId());
				
		// Non-admin user1 should be able to read a public notebook from user2
		notebookRepository.findById(publicNotebookB.getId());
		
		// Non-admin user1 should not be able to read a private notebook from user2
		try {
			notebookRepository.findById(privateNotebookB.getId());
			fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findById_adminCallingShouldReturnAllItems() throws Exception {
		
		// Admin should be able to read a public notebook from user1
		notebookRepository.findById(publicNotebookA.getId());
		
		// Admin should be able to read a private notebook from user1
		notebookRepository.findById(privateNotebookA.getId());
	}
	
	@Test
	@WithAnonymousUser
	public void findAll_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public notebooks
		Page<Notebook> result = notebookRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(2));
		result.getContent().forEach(notebook -> {
			assertThat(notebook.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findAll_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public notebooks
		Page<Notebook> result = notebookRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(notebook -> {
			assertThat((notebook.isPubliclyShared() || notebook.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findAll_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all notebooks
		Page<Notebook> result = notebookRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(4));
	}
	
	@Test
	@WithAnonymousUser
	public void findByNameContainingIgnoreCase_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public notebooks matching search criteria
		Page<Notebook> result = notebookRepository.findByNameContainingIgnoreCase("notebookA", pageable);
		assertThat(result.getContent(), hasSize(1));
		result.getContent().forEach(notebook -> {
			assertThat(notebook.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockKeycloakUser(username="user1", roles={ "user" })
	public void findByNameContainingIgnoreCase_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public notebooks matching search criteria
		Page<Notebook> result = notebookRepository.findByNameContainingIgnoreCase("notebook", pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(notebook -> {
			assertThat((notebook.isPubliclyShared() || notebook.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockKeycloakUser(username="admin", roles={ "admin" })
	public void findByNameContainingIgnoreCase_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all notebooks matching search criteria
		Page<Notebook> resultColl = notebookRepository.findByNameContainingIgnoreCase("notebook", pageable);
		assertThat(resultColl.getContent(), hasSize(4));
		Page<Notebook> resultPrivate = notebookRepository.findByNameContainingIgnoreCase("private", pageable);
		assertThat(resultPrivate.getContent(), hasSize(2));
	}
	
}
