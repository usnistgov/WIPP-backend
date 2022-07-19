package gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin;

import gov.nist.itl.ssd.wipp.backend.Application;
import gov.nist.itl.ssd.wipp.backend.app.SecurityConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.computation.Plugin;
import gov.nist.itl.ssd.wipp.backend.core.model.computation.PluginRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * <h1>Collection of tests for {@link PluginRepository}</h1>
 * <p>
 * Testing access control on CREATE, UPDATE, DELETE operations for
 * {@link PluginRepository} with REST access
 * <p>
 * Default supported HTTP methods for Spring Data REST repositories are:
 * <ul>
 * 	<li>GET and POST for the Collection Resource (/{collectionName})</li>
 * 	<li>GET, PUT, PATCH and DELETE for the Item Resource (/{collectionName}/{itemId})</li>
 * 	<li>GET for the Search Resource (/{collectionName}/search/findBy...)</li>
 * </ul>
 * Uses embedded MongoDB database and mock Keycloak users
 *
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = { Application.class, SecurityConfig.class },
        properties = { "spring.data.mongodb.port=0" })
public class PluginRepositoryRestTest {

    static final String PAYLOAD = "{\"name\": \"org/test-plugin\", \"version\": \"2.0.0\"}";

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @Autowired
    PluginRepository pluginRepository;

    Plugin plugin;

    @Before
    public void setUp() {
        mvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clear embedded database
        pluginRepository.deleteAll();

        // Create and save test plugin
        plugin = new Plugin();
        plugin.setName("org/test-plugin");
        plugin.setVersion("1.0.0");
        pluginRepository.save(plugin);
    }

    @Test
    @WithAnonymousUser
    public void rejectsPostRequestsToPluginResourceForAnonymousUser() throws Exception {

        mvc.perform(post("/api/plugins")
                        .content(PAYLOAD)
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles={"user"})
    public void rejectsPostRequestsToPluginResourceForUser() throws Exception {

        mvc.perform(post("/api/plugins")
                        .content(PAYLOAD)
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles={"admin"})
    public void acceptsPostRequestsToPluginResourceForAdmin() throws Exception {

        mvc.perform(post("/api/plugins")
                        .content(PAYLOAD)
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles={"developer"})
    public void acceptsPostRequestsToPluginResourceForDeveloper() throws Exception {

        mvc.perform(post("/api/plugins")
                        .content(PAYLOAD)
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @WithAnonymousUser
    public void rejectsPatchRequestsToPluginResourceForAnonymousUser() throws Exception {

        mvc.perform(patch("/api/plugins/" + plugin.getId())
                        .content(PAYLOAD)
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles={"user"})
    public void rejectsPatchRequestsToPluginResourceForUser() throws Exception {

        mvc.perform(patch("/api/plugins/" + plugin.getId())
                        .content(PAYLOAD)
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles={"admin"})
    public void acceptsPatchRequestsToPluginResourceForAdmin() throws Exception {

        mvc.perform(patch("/api/plugins/" + plugin.getId())
                        .content(PAYLOAD)
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles={"developer"})
    public void acceptsPatchRequestsToPluginResourceForDeveloper() throws Exception {

        mvc.perform(patch("/api/plugins/" + plugin.getId())
                        .content(PAYLOAD)
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void rejectsPutRequestsToPluginResourceForAnonymousUser() throws Exception {

        mvc.perform(put("/api/plugins/" + plugin.getId())
                        .content(PAYLOAD)
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles={"user"})
    public void rejectsPutRequestsToPluginResourceForUser() throws Exception {

        mvc.perform(put("/api/plugins/" + plugin.getId())
                        .content(PAYLOAD)
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles={"admin"})
    public void acceptsPutRequestsToPluginResourceForAdmin() throws Exception {

        mvc.perform(put("/api/plugins/" + plugin.getId())
                        .content(PAYLOAD)
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles={"developer"})
    public void acceptsPutRequestsToPluginResourceForDeveloper() throws Exception {

        mvc.perform(put("/api/plugins/" + plugin.getId())
                        .content(PAYLOAD)
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void rejectsDeleteRequestsToPluginResourceForAnonymousUser() throws Exception {

        mvc.perform(delete("/api/plugins/" + plugin.getId())
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles={"user"})
    public void rejectsDeleteRequestsToPluginResourceForUser() throws Exception {

        mvc.perform(delete("/api/plugins/" + plugin.getId())
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles={"admin"})
    public void acceptsDeleteRequestsToPluginResourceForAdmin() throws Exception {

        mvc.perform(delete("/api/plugins/" + plugin.getId())
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles={"developer"})
    public void acceptsDeleteRequestsToPluginResourceForDeveloper() throws Exception {

        mvc.perform(delete("/api/plugins/" + plugin.getId())
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNoContent());
    }

}
