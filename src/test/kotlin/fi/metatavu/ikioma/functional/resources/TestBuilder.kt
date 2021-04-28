package fi.metatavu.ikioma.functional.resources

import fi.metatavu.ikioma.email.api.client.infrastructure.ApiClient
import fi.metatavu.ikioma.integrations.test.functional.auth.TestBuilderAuthentication
import fi.metatavu.ikioma.integrations.test.functional.settings.ApiTestSettings
import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication
import fi.metatavu.jaxrs.test.functional.builder.auth.KeycloakAccessTokenProvider
import org.eclipse.microprofile.config.ConfigProvider
import java.io.IOException


/**
 * TestBuilder implementation
 *
 * @author Antti Leppä
 * @author Jari Nykänen
 */
open class TestBuilder: AbstractTestBuilder<ApiClient>() {

    val settings = ApiTestSettings()

    private var teroAyramoNonRegistered: TestBuilderAuthentication? = null
    private var teroAyramo: TestBuilderAuthentication? = null
    private var onniKorhonen: TestBuilderAuthentication? = null

    override fun createTestBuilderAuthentication(testBuilder: AbstractTestBuilder<ApiClient>, accessTokenProvider: AccessTokenProvider): AuthorizedTestBuilderAuthentication<ApiClient> {
        return TestBuilderAuthentication(this, accessTokenProvider)
    }

    /**
     * Returns authentication resource authenticated as non registered Tero Äyrämö
     *
     * @return authentication resource authenticated as non registered Tero Äyrämö
     * @throws IOException
     */
    @kotlin.jvm.Throws(IOException::class)
    fun teroAyramoNonRegistered(): TestBuilderAuthentication {
        if (teroAyramoNonRegistered == null) {
            val authServerUrl: String = ConfigProvider.getConfig().getValue("ikioma.keycloak.url", String::class.java)
            val realm: String = ConfigProvider.getConfig().getValue("ikioma.keycloak.realm", String::class.java)
            val clientId = "app"
            val username = "tero.ayramo.non.registered"
            val password = "pass"
            teroAyramoNonRegistered = TestBuilderAuthentication(this, KeycloakAccessTokenProvider(authServerUrl, realm, clientId, username, password, null))
        }

        return teroAyramoNonRegistered!!
    }

    /**
     * Returns authentication resource authenticated as Tero Äyrämö
     *
     * @return authentication resource authenticated as Tero Äyrämö
     * @throws IOException
     */
    @kotlin.jvm.Throws(IOException::class)
    fun teroAyramo(): TestBuilderAuthentication {
        if (teroAyramo == null) {
            val authServerUrl: String = ConfigProvider.getConfig().getValue("ikioma.keycloak.url", String::class.java)
            val realm: String = ConfigProvider.getConfig().getValue("ikioma.keycloak.realm", String::class.java)
            val clientId = "app"
            val username = "tero.ayramo"
            val password = "pass"
            teroAyramo = TestBuilderAuthentication(this, KeycloakAccessTokenProvider(authServerUrl, realm, clientId, username, password, null))
        }

        return teroAyramo!!
    }

    /**
     * Returns authentication resource authenticated as Onni Korhonen
     *
     * @return authentication resource authenticated as Onni Korhonen
     * @throws IOException
     */
    @kotlin.jvm.Throws(IOException::class)
    fun onniKorhonen(): TestBuilderAuthentication {
        if (onniKorhonen == null) {
            val authServerUrl: String = ConfigProvider.getConfig().getValue("ikioma.keycloak.url", String::class.java)
            val realm: String = ConfigProvider.getConfig().getValue("ikioma.keycloak.realm", String::class.java)
            val clientId = "app"
            val username = "onni.korhonen"
            val password = "pass"
            onniKorhonen = TestBuilderAuthentication(this, KeycloakAccessTokenProvider(authServerUrl, realm, clientId, username, password, null))
        }

        return onniKorhonen!!
    }

}