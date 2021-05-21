package fi.metatavu.ikioma.functional.impl

import fi.metatavu.ikioma.email.api.client.apis.CheckoutFinlandApi
import fi.metatavu.ikioma.email.api.client.infrastructure.ApiClient
import fi.metatavu.ikioma.email.api.client.infrastructure.ClientException
import fi.metatavu.ikioma.email.api.client.models.PrescriptionRenewal
import fi.metatavu.ikioma.functional.resources.TestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import org.junit.jupiter.api.fail

/**
 * Test Builder Resource for CheckoutFinland API
 */
class CheckoutFinlandTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<PrescriptionRenewal, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): CheckoutFinlandApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return CheckoutFinlandApi(testBuilder.settings.apiBasePath)
    }

    /**
     * Sends the payment success message
     *
     * @param checkoutAccount merchant id
     * @param checkoutAlgorithm algorithm
     * @param checkoutAmount amount
     * @param checkoutStamp stamp
     * @param checkoutReference reference
     * @param checkoutTransactionId transaction id
     * @param checkoutStatus status
     * @param checkoutProvider provider
     * @param signature signature
     */
    fun success(
        checkoutAccount: Int,
        checkoutAlgorithm: String,
        checkoutAmount: Int,
        checkoutStamp: String,
        checkoutReference: String,
        checkoutTransactionId: String,
        checkoutStatus: String,
        checkoutProvider: String,
        signature: String
    ) {
        return api.checkoutFinlandSuccess(
            checkoutAccount = checkoutAccount,
            checkoutAlgorithm = checkoutAlgorithm,
            checkoutAmount = checkoutAmount,
            checkoutStamp = checkoutStamp,
            checkoutReference = checkoutReference,
            checkoutTransactionId = checkoutTransactionId,
            checkoutStatus = checkoutStatus,
            checkoutProvider = checkoutProvider,
            signature = signature
        )
    }

    /**
     * Asserts that sending the payment success message fails with the expected status
     *
     * @param expectedStatus expected status
     * @param checkoutAccount merchant id
     * @param checkoutAlgorithm algorithm
     * @param checkoutAmount amount
     * @param checkoutStamp stamp
     * @param checkoutReference reference
     * @param checkoutTransactionId transaction id
     * @param checkoutStatus checkout status
     * @param checkoutProvider payment provider
     * @param signature pre calculated hmac signature of parameters above
     */
    fun assertSuccessCallFail(
        expectedStatus: Int,
        checkoutAccount: Int,
        checkoutAlgorithm: String,
        checkoutAmount: Int,
        checkoutStamp: String,
        checkoutReference: String,
        checkoutTransactionId: String,
        checkoutStatus: String,
        checkoutProvider: String,
        signature: String
    ) {
        try{
            api.checkoutFinlandSuccess(
                checkoutAccount = checkoutAccount,
                checkoutAlgorithm = checkoutAlgorithm,
                checkoutAmount = checkoutAmount,
                checkoutStamp = checkoutStamp,
                checkoutReference = checkoutReference,
                checkoutTransactionId = checkoutTransactionId,
                checkoutStatus = checkoutStatus,
                checkoutProvider = checkoutProvider,
                signature = signature
            )
        fail(String.format("Expected success call to fail with message %d", expectedStatus))
    } catch (e: ClientException) {
        assertClientExceptionStatus(expectedStatus, e)
    } catch (e: Exception) {
        fail(e.message)
    }
    }
}