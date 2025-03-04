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
package gov.nist.itl.ssd.wipp.backend.data.aimodel;

import gov.nist.itl.ssd.wipp.backend.Application;
import gov.nist.itl.ssd.wipp.backend.app.SecurityConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * Collection of tests for {@link AiModelRepository} exposed methods
 * Testing access control on READ operations
 * Uses embedded MongoDB database and mock users
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@SpringBootTest(
		classes = { Application.class, SecurityConfig.class },
		properties = { "spring.data.mongodb.port=0", "de.flapdoodle.mongodb.embedded.version=6.0.5"}
)
public class AiModelRepositoryTest {
	
	@Autowired
	AiModelRepository aiModelRepository;

	AiModel publicTensorflowModelA, publicTensorflowModelB,
			privateTensorflowModelA, privateTensorflowModelB,
			publicPytorchModelA;
	
	@BeforeEach
	public void setUp() {
		
		// Clear embedded database
		aiModelRepository.deleteAll();
		
		// Create and save publicTensorflowModelA (public: true, owner: user1)
		publicTensorflowModelA = new AiModel("publicTensorflowModelA");
		publicTensorflowModelA.setOwner("user1");
		publicTensorflowModelA.setPubliclyShared(true);
		publicTensorflowModelA.setFramework("tensorflow");
		publicTensorflowModelA = aiModelRepository.save(publicTensorflowModelA);

		// Create and save publicTensorflowModelB (public: true, owner: user2)
		publicTensorflowModelB = new AiModel("publicTensorflowModelB");
		publicTensorflowModelB.setOwner("user2");
		publicTensorflowModelB.setPubliclyShared(true);
		publicTensorflowModelB.setFramework("tensorflow");
		publicTensorflowModelB = aiModelRepository.save(publicTensorflowModelB);

		// Create and save privateTensorflowModelA (public: false, owner: user1)
		privateTensorflowModelA = new AiModel("privateTensorflowModelA");
		privateTensorflowModelA.setOwner("user1");
		privateTensorflowModelA.setPubliclyShared(false);
		privateTensorflowModelA.setFramework("tensorflow");
		privateTensorflowModelA = aiModelRepository.save(privateTensorflowModelA);

		// Create and save privateTensorflowModelB (public: false, owner: user2)
		privateTensorflowModelB = new AiModel("privateTensorflowModelB");
		privateTensorflowModelB.setOwner("user2");
		privateTensorflowModelB.setPubliclyShared(false);
		privateTensorflowModelB.setFramework("tensorflow");
		privateTensorflowModelB = aiModelRepository.save(privateTensorflowModelB);

		// Create and save publicPytorchModelA (public: true, owner: user1)
		publicPytorchModelA = new AiModel("publicPytorchModelA");
		publicPytorchModelA.setOwner("user1");
		publicPytorchModelA.setPubliclyShared(true);
		publicPytorchModelA.setFramework("pytorch");
		publicPytorchModelA = aiModelRepository.save(publicPytorchModelA);
	}
	
	@Test
	@WithAnonymousUser
	public void findById_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		// Anonymous user should be able to read a public tensorflowModel
		aiModelRepository.findById(publicTensorflowModelA.getId());
		
		// Anonymous user should not be able to read a private tensorflowModel
		try {
			aiModelRepository.findById(privateTensorflowModelA.getId());
			Assertions.fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
	
	@Test
	@WithMockUser(username="user1", roles={ "user" })
	public void findById_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		// Non-admin user1 should be able to read own private tensorflowModel
		aiModelRepository.findById(privateTensorflowModelA.getId());
				
		// Non-admin user1 should be able to read a public tensorflowModel from user2
		aiModelRepository.findById(publicTensorflowModelB.getId());
		
		// Non-admin user1 should not be able to read a private tensorflowModel from user2
		try {
			aiModelRepository.findById(privateTensorflowModelB.getId());
			Assertions.fail("Expected AccessDenied security error");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	@WithMockUser(username="admin", roles={ "admin" })
	public void findById_adminCallingShouldReturnAllItems() throws Exception {
		
		// Admin should be able to read a public aiModel from user1
		aiModelRepository.findById(publicTensorflowModelA.getId());
		
		// Admin should be able to read a private aiModel from user1
		aiModelRepository.findById(privateTensorflowModelA.getId());
	}
	
	@Test
	@WithAnonymousUser
	public void findAll_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public aiModels
		Page<AiModel> result = aiModelRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(aiModel -> {
			assertThat(aiModel.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockUser(username="user1", roles={ "user" })
	public void findAll_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public aiModels
		Page<AiModel> result = aiModelRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(4));
		result.getContent().forEach(aiModel -> {
			assertThat((aiModel.isPubliclyShared() || aiModel.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockUser(username="admin", roles={ "admin" })
	public void findAll_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all aiModels
		Page<AiModel> result = aiModelRepository.findAll(pageable);
		assertThat(result.getContent(), hasSize(5));
	}
	
	@Test
	@WithAnonymousUser
	public void findByNameContainingIgnoreCase_anonymousCallingShouldReturnOnlyPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Anonymous user should get only get list of public aiModels matching search criteria
		Page<AiModel> result = aiModelRepository.findByNameContainingIgnoreCase("tensorflowModelA", pageable);
		assertThat(result.getContent(), hasSize(1));
		result.getContent().forEach(tensorflowModel -> {
			assertThat(tensorflowModel.isPubliclyShared(), is(true));
		});
	}
	
	@Test
	@WithMockUser(username="user1", roles={ "user" })
	public void findByNameContainingIgnoreCase_nonAdminCallingShouldReturnOnlyOwnOrPublicItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Non-admin user1 should only get list of own and public aiModels matching search criteria
		Page<AiModel> result = aiModelRepository.findByNameContainingIgnoreCase("tensorflowModel", pageable);
		assertThat(result.getContent(), hasSize(3));
		result.getContent().forEach(tensorflowModel -> {
			assertThat((tensorflowModel.isPubliclyShared() || tensorflowModel.getOwner().equals("user1")), is(true));
		});
	}

	@Test
	@WithMockUser(username="admin", roles={ "admin" })
	public void findByNameContainingIgnoreCase_adminCallingShouldReturnAllItems() throws Exception {
		
		Pageable pageable = PageRequest.of(0, 10);

		// Admin should get list of all aiModels matching search criteria
		Page<AiModel> resultColl = aiModelRepository.findByNameContainingIgnoreCase("tensorflowModel", pageable);
		assertThat(resultColl.getContent(), hasSize(4));
		Page<AiModel> resultPrivate = aiModelRepository.findByNameContainingIgnoreCase("private", pageable);
		assertThat(resultPrivate.getContent(), hasSize(2));
	}

	@Test
	@WithAnonymousUser
	public void checkFrameworkValue() throws Exception {
		Assertions.assertEquals(publicTensorflowModelA.getFramework(), "tensorflow");
		Assertions.assertEquals(publicPytorchModelA.getFramework(), "pytorch");
	}

}
