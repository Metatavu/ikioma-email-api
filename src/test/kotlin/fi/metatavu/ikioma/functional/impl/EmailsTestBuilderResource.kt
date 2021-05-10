package fi.metatavu.ikioma.functional.impl

import fi.metatavu.ikioma.email.api.client.apis.EmailApi
import fi.metatavu.ikioma.email.api.client.infrastructure.ApiClient
import fi.metatavu.ikioma.email.api.client.infrastructure.ClientException
import fi.metatavu.ikioma.email.api.spec.model.Email
import fi.metatavu.ikioma.functional.resources.TestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.fail

/**
 * Test Builder Resource for Emails API
 */
class EmailsTestBuilderResource (testBuilder: TestBuilder,
                                 private val accessTokenProvider: AccessTokenProvider?,
                                 apiClient: ApiClient
):ApiTestBuilderResource<Email, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): EmailApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return EmailApi(testBuilder.settings.apiBasePath)
    }

    /**
     * Sends email
     *
     * @param payload email
     */
    fun sendEmail(payload: fi.metatavu.ikioma.email.api.client.models.Email) {
        api.createEmail(payload);
    }

    /**
     * Asserts that sending email fails with given status
     *
     * @param status expected status
     * @param payload email
     */
    fun assertSendFail(status: Int, payload: fi.metatavu.ikioma.email.api.client.models.Email) {
        try {
            api.createEmail(payload)
            fail(String.format("Expected send to fail with message %d", status))
        } catch (e: ClientException) {
            assertEquals(status, e.statusCode)
        }
    }
}
