package fi.metatavu.ikioma.functional.impl

import fi.metatavu.ikioma.email.api.client.apis.EmailApi
import fi.metatavu.ikioma.email.api.client.infrastructure.ApiClient
import fi.metatavu.ikioma.email.api.spec.model.Email
import fi.metatavu.ikioma.functional.resources.TestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import org.testcontainers.shaded.org.bouncycastle.crypto.agreement.jpake.JPAKERound1Payload

class EmailsTestBuilderResource (testBuilder: TestBuilder,
                                 private val accessTokenProvider: AccessTokenProvider?,
                                 apiClient: ApiClient
):ApiTestBuilderResource<Email, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): EmailApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return EmailApi(testBuilder.settings.apiBasePath)
    }

    fun sendEmail(payload: fi.metatavu.ikioma.email.api.client.models.Email) {
        return api.createEmail(payload);
    }

}
