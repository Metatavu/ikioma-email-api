package fi.metatavu.ikioma.integrations.test.functional.resources

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager

/**
 * Class for Keycloak test resource.
 *
 * @author Jari Nykänen
 */
class KeycloakTestResource: QuarkusTestResourceLifecycleManager {

    override fun start(): MutableMap<String, String> {
        keycloak.start()
        val config: MutableMap<String, String> = HashMap()
        config["quarkus.oidc.auth-server-url"] = java.lang.String.format("%s/realms/test", keycloak.authServerUrl)
        config["quarkus.oidc.client-id"] = "api"
        config["quarkus.oidc.credentials.secret"] = "041e7b11-70a1-4beb-a59a-c1de3fde1cf8"

        config["ikioma.keycloak.url"] = keycloak.authServerUrl
        config["ikioma.keycloak.realm"] = "test"

        config["ikioma.keycloak.api-admin.secret"] = "ce716463-396d-4706-866d-2da5c0cdb35a"
        config["ikioma.keycloak.api-admin.client"] = "admin-client"
        config["ikioma.keycloak.api-admin.password"] = "70cc3724-c0e7-42d8-8334-c1acb1eb7742"
        config["ikioma.keycloak.api-admin.user"] = "api-admin"
        return config
    }

    override fun stop() {
        keycloak.stop()
    }

    companion object {
        val keycloak: KeycloakContainer = KeycloakContainer()
            .withRealmImportFile("kc.json")
    }
}