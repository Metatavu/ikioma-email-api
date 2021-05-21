package fi.metatavu.ikioma.functional.tests

import fi.metatavu.ikioma.email.api.client.models.PaymentStatus
import fi.metatavu.ikioma.email.api.client.models.PrescriptionRenewal
import fi.metatavu.ikioma.functional.resources.MysqlResource
import fi.metatavu.ikioma.functional.resources.TestBuilder
import fi.metatavu.ikioma.integrations.test.functional.resources.KeycloakTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import org.eclipse.microprofile.config.ConfigProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests Payments API
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlResource::class)
)
class PrescriptionRenewalsTestsIT {

    /**
     * Tests creating prescription renewal request and approving it with:
     *  -checkout request where one field differs form the database
     *  -checkout request where signature is incorrect
     *  -correct checkout request
     *  and verifies that after successful payment the prescription renewal object is removed from the database
     */
    @Test
    fun prescriptionRenewal() {
        TestBuilder().use { builder ->
            val practitionerId = UUID.randomUUID()

            val createdPrescription = builder.teroAyramo().prescriptionRenewals.create(
                PrescriptionRenewal(
                    status = PaymentStatus.nOTPAID,
                    practitionerUserId = practitionerId,
                    prescriptions = arrayOf("Burana", "Even more Burana", "All the burana")
                )
            )

            Assertions.assertNotNull(createdPrescription.transactionId)
            Assertions.assertNotNull(createdPrescription.paymentUrl)
            Assertions.assertNotNull(createdPrescription.id)
            Assertions.assertNotNull(createdPrescription.checkoutAccount)

            val foundRenewalRequest = builder.teroAyramo().prescriptionRenewals.find(createdPrescription.id!!)
            Assertions.assertEquals(createdPrescription.transactionId, foundRenewalRequest.transactionId)
            Assertions.assertEquals(createdPrescription.practitionerUserId, foundRenewalRequest.practitionerUserId)
            Assertions.assertEquals(createdPrescription.status, foundRenewalRequest.status)
            Assertions.assertEquals(3, foundRenewalRequest.prescriptions.size)

            val algorithm = "sha256"
            val provider = "spankki"
            val status = "ok"
            val amount = 1

            builder.teroAyramo().checkoutFinlandTestBuilderResource.assertSuccessCallFail(
                403,
                checkoutAccount = foundRenewalRequest.checkoutAccount!!,
                checkoutAlgorithm = algorithm,
                checkoutProvider = provider,
                checkoutStamp = UUID.randomUUID().toString(),
                checkoutReference = foundRenewalRequest.id!!.toString(),
                checkoutAmount = amount,
                checkoutStatus = status,
                checkoutTransactionId = foundRenewalRequest.transactionId!!,
                signature = UUID.randomUUID().toString()
            )

            builder.teroAyramo().checkoutFinlandTestBuilderResource.assertSuccessCallFail(
                403,
                checkoutAccount = foundRenewalRequest.checkoutAccount!!,
                checkoutAlgorithm = algorithm,
                checkoutProvider = provider,
                checkoutStamp = foundRenewalRequest.stamp!!.toString(),
                checkoutReference = foundRenewalRequest.id!!.toString(),
                checkoutAmount = amount,
                checkoutStatus = status,
                checkoutTransactionId = foundRenewalRequest.transactionId!!,
                signature = UUID.randomUUID().toString()
            )

            builder.teroAyramo().checkoutFinlandTestBuilderResource.success(
                checkoutAccount = foundRenewalRequest.checkoutAccount,
                checkoutAlgorithm = algorithm,
                checkoutProvider = provider,
                checkoutStamp = foundRenewalRequest.stamp.toString(),
                checkoutReference = foundRenewalRequest.id.toString(),
                checkoutAmount = amount,
                checkoutStatus = status,
                checkoutTransactionId = foundRenewalRequest.transactionId,
                signature = calculateHmac(
                    checkoutAccount = foundRenewalRequest.checkoutAccount,
                    status = status,
                    amount = amount,
                    provider = provider,
                    stamp = foundRenewalRequest.stamp,
                    reference = foundRenewalRequest.id,
                    transactionId = foundRenewalRequest.transactionId
                )
            )

            builder.teroAyramo().prescriptionRenewals.assertFindFailStatus(404, createdPrescription.id)
        }
    }

    /**
     * Calculates HMAC for test checkout success message with some values predefined
     */
    private fun calculateHmac(
        checkoutAccount: Int,
        status: String,
        amount: Int,
        provider: String,
        stamp: UUID,
        reference: UUID,
        transactionId: String
    ): String {
        val sw = StringBuilder()
        sw.append("checkout-account:${checkoutAccount}\n")
        sw.append("checkout-algorithm:sha256\n")
        sw.append("checkout-amount:$amount\n")
        sw.append("checkout-provider:$provider\n")
        sw.append("checkout-reference:${reference}\n")
        sw.append("checkout-stamp:${stamp}\n")
        sw.append("checkout-status:$status\n")
        sw.append("checkout-transaction-id:${transactionId}\n")

        return HmacUtils(
            HmacAlgorithms.HMAC_SHA_256,
            ConfigProvider.getConfig().getValue("checkout.merchant.secret", String::class.java)
        ).hmacHex(sw.toString())
    }
}