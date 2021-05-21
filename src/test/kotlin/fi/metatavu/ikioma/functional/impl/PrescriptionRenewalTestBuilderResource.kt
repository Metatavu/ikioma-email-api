package fi.metatavu.ikioma.functional.impl

import fi.metatavu.ikioma.email.api.client.apis.PrescriptionRenewalsApi
import fi.metatavu.ikioma.email.api.client.infrastructure.ApiClient
import fi.metatavu.ikioma.email.api.client.infrastructure.ClientException
import fi.metatavu.ikioma.email.api.client.models.PrescriptionRenewal
import fi.metatavu.ikioma.functional.resources.TestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import org.junit.jupiter.api.fail
import java.util.*

/**
 * Test Builder Resource for Prescription renewals API
 */
class PrescriptionRenewalTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<PrescriptionRenewal, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): PrescriptionRenewalsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return PrescriptionRenewalsApi(testBuilder.settings.apiBasePath)
    }

    /**
     * Creates new prescription renewal request
     *
     * @param prescriptionRenewal prescriptionRenewal request
     * @return created entity
     */
    fun create(prescriptionRenewal: PrescriptionRenewal): PrescriptionRenewal {
        return api.createPrescriptionRenewal(prescriptionRenewal)
    }

    /**
     * Returns prescription renewal by id
     *
     * @param id id
     * @return found entity
     */
    fun find(id: UUID): PrescriptionRenewal {
        return api.getPrescriptionRenewal(id)
    }

    /**
     * Asserts failure to find prescription renewal request by id
     *
     * @param expectedStatus expected status
     * @param id id
     */
    fun assertFindFailStatus(
        expectedStatus: Int,
        id: UUID
    ) {
        try {
            api.getPrescriptionRenewal(id)
            fail(String.format("Expected find call to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        } catch (e: Exception) {
            fail(e.message)
        }
    }
}
