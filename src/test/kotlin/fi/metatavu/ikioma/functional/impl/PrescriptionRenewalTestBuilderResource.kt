package fi.metatavu.ikioma.functional.impl

import fi.metatavu.ikioma.email.api.client.apis.PrescriptionRenewalsApi
import fi.metatavu.ikioma.email.api.client.infrastructure.ApiClient
import fi.metatavu.ikioma.email.api.client.models.PrescriptionRenewal
import fi.metatavu.ikioma.functional.impl.ApiTestBuilderResource
import fi.metatavu.ikioma.functional.resources.TestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.fail
import java.util.*

/**
 * Test Builder Resource for Emails API
 */
class PrescriptionRenewalTestBuilderResource (testBuilder: TestBuilder,
                                 private val accessTokenProvider: AccessTokenProvider?,
                                 apiClient: ApiClient
): ApiTestBuilderResource<PrescriptionRenewal, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): PrescriptionRenewalsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return PrescriptionRenewalsApi(testBuilder.settings.apiBasePath)
    }

    fun create(prescriptionRenewal: PrescriptionRenewal): PrescriptionRenewal {
        return api.createPrescriptionRenewal(prescriptionRenewal)
    }

    fun find(id: UUID): PrescriptionRenewal {
        return api.getPrescriptionRenewal(id)
    }
}
