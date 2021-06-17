package fi.metatavu.ikioma.functional.auth

import fi.metatavu.ikioma.email.api.client.infrastructure.ApiClient
import fi.metatavu.ikioma.functional.impl.PrescriptionRenewalTestBuilderResource
import fi.metatavu.ikioma.functional.resources.TestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication

/**
 * Test builder authentication
 *
 * @author Antti Leppä
 * @author Jari Nykänen
 *
 * Constructor
 *
 * @param testBuilder test builder instance
 * @param accessTokenProvider access token provider
 */
class TestBuilderAuthentication(
    private val testBuilder: TestBuilder,
    accessTokenProvider: AccessTokenProvider
):AuthorizedTestBuilderAuthentication<ApiClient>(testBuilder, accessTokenProvider) {

    private var accessTokenProvider: AccessTokenProvider? = accessTokenProvider

    var prescriptionRenewals: PrescriptionRenewalTestBuilderResource = PrescriptionRenewalTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())

    /**
     * Creates a API client
     *
     * @param accessToken access token
     * @return API client
     */
    override fun createClient(accessToken: String): ApiClient {
        val result = ApiClient(testBuilder.settings.apiBasePath)
        ApiClient.accessToken = accessToken
        return result
    }

}