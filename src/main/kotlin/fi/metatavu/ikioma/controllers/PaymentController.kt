package fi.metatavu.ikioma.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.ikioma.email.api.spec.model.PrescriptionRenewal
import fi.metatavu.ikioma.email.payment.api.spec.PaymentsApi
import fi.metatavu.ikioma.email.payment.spec.model.Callbacks
import fi.metatavu.ikioma.email.payment.spec.model.Customer
import fi.metatavu.ikioma.email.payment.spec.model.Item
import fi.metatavu.ikioma.email.payment.spec.model.PaymentRequest
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.openapitools.client.infrastructure.Serializer
import org.slf4j.Logger
import java.io.StringWriter
import java.net.URI
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller class for managing payments
 */
@ApplicationScoped
class PaymentController {

    @Inject
    @ConfigProperty(name = "checkout.algorithm")
    private lateinit var algorithm: String

    @Inject
    @ConfigProperty(name = "checkout.merchant.id")
    private var merchantId: Int = 0

    @Inject
    @ConfigProperty(name = "checkout.redirectBaseUrl")
    private lateinit var redirectBaseUrl: String

    @Inject
    @ConfigProperty(name = "checkout.merchant.secret")
    private lateinit var merchantSecret: String

    @Inject
    private lateinit var keycloakController: KeycloakController

    @Inject
    private lateinit var logger: Logger

    /**
     * Sends the payment request to Checkout Finland
     *
     * @param prescriptionRenewal original renewal object
     * @param loggedInUser user id
     * @return filled prescription renewal
     * */
    fun initRenewPrescriptionPayment(
        prescriptionRenewal: PrescriptionRenewal,
        loggedInUser: UUID
    ): PrescriptionRenewal? {
        val nonce = UUID.randomUUID().toString()
        val timestamp = OffsetDateTime.now()
        val reference = UUID.randomUUID().toString()
        val stamp = UUID.randomUUID().toString()

        val customer = buildCustomerObject(loggedInUser)
        customer ?: return null

        val paymentsApi = PaymentsApi()
        val paymentRequest = PaymentRequest(
            stamp = stamp,
            reference = reference,
            amount = 2,
            customer = customer,
            currency = PaymentRequest.Currency.eUR,
            language = PaymentRequest.Language.eN,
            items = listOf(
                Item(
                    productCode = "111",
                    category = "health",
                    vatPercentage = 22,
                    unitPrice = 2,
                    units = 1,
                    deliveryDate = LocalDate.now()
                )
            ),
            callbackUrls = Callbacks(
                success = "$redirectBaseUrl/v1/checkoutFinland/success",
                cancel = "$redirectBaseUrl/v1/checkoutFinland/cancel"
            ),
            redirectUrls = Callbacks(
                success = "$redirectBaseUrl/v1/checkoutFinland/success",
                cancel = "$redirectBaseUrl/v1/checkoutFinland/cancel"
            )
        )
        logger.info("Payment with stamp $stamp is created")

        val headers = mapOf(
            Pair("checkout-account", merchantId),
            Pair("checkout-algorithm", algorithm),
            Pair("checkout-method", "POST"),
            Pair("checkout-nonce", nonce),
            Pair("checkout-timestamp", timestamp)
        )

        val sendPayment = paymentsApi.createPayment(
            paymentRequest,
            headers["checkout-account"] as Int?,
            headers["checkout-algorithm"] as String?,
            headers["checkout-method"] as String?,
            headers["checkout-timestamp"] as OffsetDateTime?,
            headers["checkout-nonce"] as String?,
            buildHMAC(headers, paymentRequest)
        )

        prescriptionRenewal.transactionId = sendPayment.transactionId.toString()
        prescriptionRenewal.paymentUrl = URI.create(sendPayment.href)
        return prescriptionRenewal
    }

    /**
     * Fills Customer object from existing keycloak data
     *
     * @param loggedInUser user id
     * @return Customer
     */
    private fun buildCustomerObject(loggedInUser: UUID): Customer? {
        val email = keycloakController.getUserEmail(loggedInUser)
        email ?: return null
        return Customer(
            email = email,
            firstName = keycloakController.getFirstName(loggedInUser),
            lastName = keycloakController.getLastName(loggedInUser)
        )
    }

    /**
     * Builds the HMAC from headers and payment request body
     *
     * @param headers map of headers
     * @param body payment request
     * @return HMAC string
     */
    private fun buildHMAC(headers: Map<String, Any>, body: PaymentRequest): String? {
        val sw = StringWriter()
        headers.toSortedMap().forEach { (a, b) ->
            run {
                sw.append("$a:$b\n")
            }
        }
        sw.append(Serializer.moshi.adapter(PaymentRequest::class.java).toJson(body))
        return if (algorithm == "sha256") {
            HmacUtils(HmacAlgorithms.HMAC_SHA_256, merchantSecret).hmacHex(sw.toString())
        } else {
            HmacUtils(HmacAlgorithms.HMAC_SHA_512, merchantSecret).hmacHex(sw.toString())
        }
    }


}