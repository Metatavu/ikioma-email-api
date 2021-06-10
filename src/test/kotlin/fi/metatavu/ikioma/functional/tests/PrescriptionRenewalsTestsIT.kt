package fi.metatavu.ikioma.functional.tests

import fi.metatavu.ikioma.email.api.client.models.PaymentStatus
import fi.metatavu.ikioma.email.api.client.models.PrescriptionRenewal
import fi.metatavu.ikioma.functional.resources.MysqlResource
import fi.metatavu.ikioma.functional.resources.TestBuilder
import fi.metatavu.ikioma.integrations.test.functional.resources.KeycloakTestResource
import fi.metatavu.ikioma.integrations.test.functional.settings.ApiTestSettings
import io.quarkus.mailer.MockMailbox
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import org.eclipse.microprofile.config.ConfigProvider
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Inject

/**
 * Tests Payments API
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlResource::class)
)
class PrescriptionRenewalsTestsIT {

    private val korhonenId = UUID.fromString("2d42e574-2670-4855-8169-da642e0ef067")
    private val korhonenEmail = "onni.korhonen@example.com"

    @Inject
    private lateinit var mailbox: MockMailbox

    @BeforeEach
    fun init() {
        mailbox.clear()
    }

    @Test
    fun prescriptionRenewalWrongRole() {
        TestBuilder().use { builder ->
            val createdPrescription = builder.teroAyramoNonRegistered().prescriptionRenewals.assertCreateFailStatus(
                403,
                PrescriptionRenewal(
                    status = PaymentStatus.nOTPAID,
                    price = 100,
                    redirectUrl = "https://example.fi/v1/checkoutFinland",
                    practitionerUserId = korhonenId,
                    prescriptions = arrayOf("Burana", "Even more Burana", "All the Burana")
                )
            )
        }
    }

    @Test
    fun prescriptionRenewalNoPractitioner() {
        TestBuilder().use { builder ->
            builder.teroAyramo().prescriptionRenewals.assertCreateFailStatus(
                404,
                PrescriptionRenewal(
                    status = PaymentStatus.nOTPAID,
                    price = 100,
                    redirectUrl = "https://example.fi/v1/checkoutFinland",
                    practitionerUserId = UUID.randomUUID(),
                    prescriptions = arrayOf("Burana", "Even more Burana", "All the Burana")
                )
            )
        }
    }

    /**
     * Tests creating prescription renewal request and approving it with:
     *  -checkout request where one field differs form the database
     *  -checkout request where signature is incorrect
     *  -correct checkout request
     *  and verifies that after successful payment the prescription renewal object is removed from the database and
     *  the email to practitioner was sent
     */
    @Test
    fun prescriptionRenewal() {
        TestBuilder().use { builder ->
            //todo this test assumes Onni korhonen to be practitioner
            val prescriptionRenewal = PrescriptionRenewal(
                status = PaymentStatus.nOTPAID,
                price = 1200,
                redirectUrl = "https://example.fi/v1/checkoutFinland",
                practitionerUserId = korhonenId,
                prescriptions = arrayOf("Burana", "Even more Burana", "All the Burana")
            )
            val createdPrescription = builder.teroAyramo().prescriptionRenewals.create(
                prescriptionRenewal
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

            //different field
            RestAssured.given()
                .queryParam("signature", UUID.randomUUID().toString())
                .queryParams(
                    mapOf<String, Any>(
                        Pair("checkout-account", foundRenewalRequest.checkoutAccount!!),
                        Pair("checkout-amount", amount),
                        Pair("checkout-provider", provider),
                        Pair("checkout-algorithm", algorithm),
                        Pair("checkout-reference", foundRenewalRequest.id!!.toString()),
                        Pair("checkout-status", status),
                        Pair("checkout-stamp", UUID.randomUUID().toString()),
                        Pair("checkout-transaction-id", foundRenewalRequest.transactionId!!)
                    )
                )
                .`when`()
                .get("${builder.settings.apiBasePath}/v1/checkoutFinland/success")
                .then().assertThat().statusCode(403).and().body("message", equalTo("Payment information does not match"))

            //wrong signature
            RestAssured.given()
                .queryParam("signature", UUID.randomUUID().toString())
                .queryParams(
                    mapOf<String, Any>(
                        Pair("checkout-account", foundRenewalRequest.checkoutAccount),
                        Pair("checkout-amount", amount),
                        Pair("checkout-provider", provider),
                        Pair("checkout-algorithm", algorithm),
                        Pair("checkout-reference", foundRenewalRequest.id.toString()),
                        Pair("checkout-status", status),
                        Pair("checkout-stamp", foundRenewalRequest.stamp!!.toString()),
                        Pair("checkout-transaction-id", foundRenewalRequest.transactionId)
                    )
                )
                .`when`()
                .get("${builder.settings.apiBasePath}/v1/checkoutFinland/success")
                .then().assertThat().statusCode(403).and().body("message", equalTo("Bad signature"))

            //ok
            RestAssured.given()
                .queryParam("signature", calculateHmac(
                    checkoutAccount = foundRenewalRequest.checkoutAccount,
                    status = status,
                    amount = amount,
                    provider = provider,
                    stamp = foundRenewalRequest.stamp,
                    reference = foundRenewalRequest.id,
                    transactionId = foundRenewalRequest.transactionId
                ))
                .queryParams(mapOf<String, Any>(
                    Pair("checkout-account", foundRenewalRequest.checkoutAccount),
                    Pair("checkout-amount", amount),
                    Pair("checkout-provider", provider),
                    Pair("checkout-algorithm", algorithm),
                    Pair("checkout-reference", foundRenewalRequest.id.toString()),
                    Pair("checkout-status", status),
                    Pair("checkout-stamp", foundRenewalRequest.stamp),
                    Pair("checkout-transaction-id", foundRenewalRequest.transactionId)
                ))
                .`when`()
                .get("${builder.settings.apiBasePath}/v1/checkoutFinland/success")
                .then().assertThat().statusCode(200)


            builder.teroAyramo().prescriptionRenewals.assertFindFailStatus(404, createdPrescription.id)
            val practitionerMessages = mailbox.getMessagesSentTo(korhonenEmail)

            Assertions.assertNotNull(practitionerMessages)
            Assertions.assertEquals(1, practitionerMessages.size)
            Assertions.assertEquals("SEC reseptinuusintapyyntö", practitionerMessages[0].subject)
            Assertions.assertTrue(practitionerMessages[0].text.startsWith("Henkilö Tero Testi Äyrämö (010170-999R) pyytää reseptin uusintaa resepteille "))
            Assertions.assertTrue(practitionerMessages[0].text.contains("Even more Burana"))
            Assertions.assertTrue(practitionerMessages[0].text.contains("All the Burana"))
        }
    }

    /**
     * Tests prescription renewal request when the payment was calcelled
     */
    @Test
    fun prescriptionRenewalPaymentCancel() {
        TestBuilder().use { builder ->
            //todo this test assumes Onni korhonen to be practitioner
            val prescriptionRenewal = PrescriptionRenewal(
                status = PaymentStatus.nOTPAID,
                price = 1200,
                redirectUrl = "https://example.fi/v1/checkoutFinland",
                practitionerUserId = korhonenId,
                prescriptions = arrayOf("Burana", "Even more Burana", "All the Burana")
            )

            val createdPrescription = builder.teroAyramo().prescriptionRenewals.create(
                prescriptionRenewal
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

            //different field
            RestAssured.given()
                .queryParam("signature", UUID.randomUUID().toString())
                .queryParams(
                    mapOf<String, Any>(
                        Pair("checkout-account", foundRenewalRequest.checkoutAccount!!),
                        Pair("checkout-amount", amount),
                        Pair("checkout-provider", provider),
                        Pair("checkout-algorithm", algorithm),
                        Pair("checkout-reference", foundRenewalRequest.id!!.toString()),
                        Pair("checkout-status", status),
                        Pair("checkout-stamp", UUID.randomUUID().toString()),
                        Pair("checkout-transaction-id", foundRenewalRequest.transactionId!!)
                    )
                )
                .`when`()
                .get("${builder.settings.apiBasePath}/v1/checkoutFinland/success")
                .then().assertThat().statusCode(403).and().body("message", equalTo("Payment information does not match"))

            //wrong signature
            RestAssured.given()
                .queryParam("signature", UUID.randomUUID().toString())
                .queryParams(
                    mapOf<String, Any>(
                        Pair("checkout-account", foundRenewalRequest.checkoutAccount),
                        Pair("checkout-amount", amount),
                        Pair("checkout-provider", provider),
                        Pair("checkout-algorithm", algorithm),
                        Pair("checkout-reference", foundRenewalRequest.id.toString()),
                        Pair("checkout-status", status),
                        Pair("checkout-stamp", foundRenewalRequest.stamp!!.toString()),
                        Pair("checkout-transaction-id", foundRenewalRequest.transactionId)
                    )
                )
                .`when`()
                .get("${builder.settings.apiBasePath}/v1/checkoutFinland/success")
                .then().assertThat().statusCode(403).and().body("message", equalTo("Bad signature"))

            //ok
            RestAssured.given()
                .queryParam(
                    "signature", calculateHmac(
                        checkoutAccount = foundRenewalRequest.checkoutAccount,
                        status = status,
                        amount = amount,
                        provider = provider,
                        stamp = foundRenewalRequest.stamp,
                        reference = foundRenewalRequest.id,
                        transactionId = foundRenewalRequest.transactionId
                    )
                )
                .queryParams(
                    mapOf<String, Any>(
                        Pair("checkout-account", foundRenewalRequest.checkoutAccount),
                        Pair("checkout-amount", amount),
                        Pair("checkout-provider", provider),
                        Pair("checkout-algorithm", algorithm),
                        Pair("checkout-reference", foundRenewalRequest.id.toString()),
                        Pair("checkout-status", status),
                        Pair("checkout-stamp", foundRenewalRequest.stamp),
                        Pair("checkout-transaction-id", foundRenewalRequest.transactionId)
                    )
                )
                .`when`()
                .get("${builder.settings.apiBasePath}/v1/checkoutFinland/cancel")
                .then().assertThat().statusCode(204)
        }
    }

    /**
     * Calculates HMAC 256 for test checkout success message with some values predefined
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