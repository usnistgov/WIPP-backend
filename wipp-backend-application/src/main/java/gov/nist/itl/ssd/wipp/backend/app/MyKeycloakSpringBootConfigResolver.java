package gov.nist.itl.ssd.wipp.backend.app;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * This class is needed for Keycloak to work because of a bug in Keycloak : see https://stackoverflow.com/questions/57787768/issues-running-example-keycloak-spring-boot-app
 */

@Configuration
public class MyKeycloakSpringBootConfigResolver extends KeycloakSpringBootConfigResolver {

    @Autowired
    private KeycloakSpringBootProperties properties;

    private KeycloakDeployment keycloakDeployment;

    @Override
    public KeycloakDeployment resolve(HttpFacade.Request facade) {
        if (keycloakDeployment != null) {
            return keycloakDeployment;
        }

        keycloakDeployment = KeycloakDeploymentBuilder.build(properties);
        return keycloakDeployment;
    }
}