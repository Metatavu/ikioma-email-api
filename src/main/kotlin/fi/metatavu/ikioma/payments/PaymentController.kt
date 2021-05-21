package fi.metatavu.ikioma.payments

import fi.metatavu.ikioma.keycloak.KeycloakController
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
import java.math.BigDecimal
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
    @ConfigProperty(name = "checkout.item.code")
    private lateinit var itemCode: String

    @Inject
    @ConfigProperty(name = "checkout.item.category")
    private lateinit var itemCategory: String

    @Inject
    @ConfigProperty(name = "checkout.item.vatPercentage")
    private var itemVAT: Long = 0

    @Inject
    @ConfigProperty(name = "checkout.item.unitPrice")
    private var price: Long = 0

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
        val reference = UUID.randomUUID()
        val stamp = UUID.randomUUID()

        val customer = buildCustomerObject(loggedInUser)
        customer ?: return null

        val paymentsApi = PaymentsApi()
        val paymentRequest = PaymentRequest(
            stamp = stamp.toString(),
            reference = reference.toString(),
            amount = price,
            customer = customer,
            currency = PaymentRequest.Currency.eUR,
            language = PaymentRequest.Language.fI,
            items = listOf(
                Item(
                    productCode = itemCode,
                    category = itemCategory,
                    vatPercentage = itemVAT,
                    unitPrice = price,
                    units = 1,
                    deliveryDate = LocalDate.now()
                )
            ),
            callbackUrls = Callbacks(
                success = "$redirectBaseUrl/success",
                cancel = "$redirectBaseUrl/cancel"
            ),
            redirectUrls = Callbacks(
                success = "$redirectBaseUrl/success",
                cancel = "$redirectBaseUrl/cancel"
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
            buildHMAC(headers, Serializer.moshi.adapter(PaymentRequest::class.java).toJson(paymentRequest))
        )

        prescriptionRenewal.id = reference
        prescriptionRenewal.stamp = stamp
        prescriptionRenewal.checkoutAccount = merchantId
        prescriptionRenewal.transactionId = sendPayment.transactionId.toString()
        prescriptionRenewal.paymentUrl = sendPayment.href
        return prescriptionRenewal
    }

    /**
     * Verifies payment based on the received parameters
     *
     * @param signature provided signature
     * @param map parameters
     * @return true if signatures are same
     */
    fun verifyPayment(signature: String, map: Map<String, String>): Boolean {
        val calculatedSignature = buildHMAC(map, "")
        return calculatedSignature == signature
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
     * Builds the HMAC from headers or parameters and request body
     *
     * @param map map of headers or string parameters
     * @param body payment request
     * @return HMAC string
     */
    private fun buildHMAC(map: Map<String, Any>, body: String): String? {
        val stringBuilder = StringBuilder()
        map.toSortedMap().forEach { (a, b) ->
            run {
                stringBuilder.append("$a:$b\n")
            }
        }
        stringBuilder.append(body)

        return when (algorithm) {
            "sha256" -> {
                HmacUtils(HmacAlgorithms.HMAC_SHA_256, merchantSecret).hmacHex(stringBuilder.toString())
            }
            "sha512" -> {
                HmacUtils(HmacAlgorithms.HMAC_SHA_512, merchantSecret).hmacHex(stringBuilder.toString())
            }
            else -> null
        }
    }

}