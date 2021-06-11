package fi.metatavu.ikioma.rest

import fi.metatavu.ikioma.email.EmailController
import fi.metatavu.ikioma.keycloak.KeycloakController
import fi.metatavu.ikioma.payments.PaymentController
import fi.metatavu.ikioma.prescriptions.PrescriptionRenewalController
import fi.metatavu.ikioma.email.api.api.spec.V1Api
import fi.metatavu.ikioma.email.api.spec.model.PaymentStatus
import fi.metatavu.ikioma.email.api.spec.model.PrescriptionRenewal
import fi.metatavu.ikioma.rest.translate.PrescriptionRenewalTranslator
import org.slf4j.Logger
import java.lang.IllegalArgumentException
import java.util.*
import javax.annotation.security.RolesAllowed
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.validation.constraints.NotNull
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Response


/**
 * V1 API
 */
@RequestScoped
@Transactional
class V1ApiImpl : V1Api, AbstractApi() {

    @Inject
    private lateinit var emailController: EmailController

    @Inject
    private lateinit var prescriptionController: PrescriptionRenewalController

    @Inject
    private lateinit var paymentController: PaymentController

    @Inject
    private lateinit var prescriptionRenewalTranslator: PrescriptionRenewalTranslator

    @Inject
    private lateinit var keycloakController: KeycloakController

    override fun checkoutFinlandCancel(signature: String): Response? {
        println("Got cancel callback")
        val checkoutParameters = getCheckoutParameters()

        println(checkoutParameters["checkout-reference"])
        val checkoutReference = checkoutParameters["checkout-reference"] ?: return createBadRequest("No checkout reference")
        println(checkoutParameters["checkout-status"])
        val checkoutStatus = checkoutParameters["checkout-status"] ?: return createBadRequest("No checkout status")
        println(checkoutParameters["checkout-stamp"])
        val checkoutStamp = checkoutParameters["checkout-stamp"] ?: return createBadRequest("No checkout stamp")
        println(checkoutParameters["checkout-transaction-id"])
        val checkoutTransactionId = checkoutParameters["checkout-transaction-id"] ?: return createBadRequest("No checkout transaction id")

        val refNo: UUID?
        try {
            refNo = UUID.fromString(checkoutReference)
        } catch (e: IllegalArgumentException) {
            println("Invalid reference no")
            return createBadRequest("Invalid reference no")
        }

        val prescriptionRenewal = prescriptionController.findPrescriptionRenewalByReference(reference = refNo)
        println("prescriptionRenewal: $prescriptionRenewal")
        prescriptionRenewal ?: return createNotFound()
        println("prescriptionRenewal.stamp: ${prescriptionRenewal.stamp}")
        println("UUID.fromString(checkoutStamp): ${UUID.fromString(checkoutStamp)}")
        println("prescriptionRenewal.transactionId: ${prescriptionRenewal.transactionId}")
        if (prescriptionRenewal.stamp != UUID.fromString(checkoutStamp) || prescriptionRenewal.transactionId != checkoutTransactionId) {
            println("Payment information does not match")
            return createForbidden("Payment information does not match")
        }

        if (!paymentController.verifyPayment(signature, checkoutParameters)) {
            println("Bad signature")
            return createForbidden("Bad signature")
        }

        println("END...")

        prescriptionController.deletePrescriptionRenewal(prescriptionRenewal)
        return createNoContent()
    }

    override fun checkoutFinlandSuccess(
        signature: String
    ): Response {
        println("Got successfult callback")
        val checkoutParameters = getCheckoutParameters()

        val checkoutReference = checkoutParameters["checkout-reference"] ?: return createBadRequest("No checkout reference")
        val checkoutStatus = checkoutParameters["checkout-status"] ?: return createBadRequest("No checkout status")
        val checkoutStamp = checkoutParameters["checkout-stamp"] ?: return createBadRequest("No checkout stamp")
        val checkoutTransactionId = checkoutParameters["checkout-transaction-id"] ?: return createBadRequest("No checkout transaction id")

        val refNo: UUID?
        try {
            refNo = UUID.fromString(checkoutReference)
        } catch (e: IllegalArgumentException) {
            return createBadRequest("Invalid reference no")
        }

        val prescriptionRenewal = prescriptionController.findPrescriptionRenewalByReference(reference = refNo)
        println("Found corresponding prescription renewal object")
        prescriptionRenewal ?: return createNotFound()
        val userId = prescriptionRenewal.creatorId

        val ssn = keycloakController.getUserSSN(userId) ?: return createNotFound("User with ID $userId could not be found!")
        val firstName = keycloakController.getFirstName(userId) ?: return createNotFound("User with ID $userId could not be found!")
        val lastName = keycloakController.getLastName(userId) ?: return createNotFound("User with ID $userId could not be found!")

        if (prescriptionRenewal.stamp != UUID.fromString(checkoutStamp) || prescriptionRenewal.transactionId != checkoutTransactionId) {
            return createForbidden("Payment information does not match")
        }

        println("payment information is valid")
        if (!paymentController.verifyPayment(signature, checkoutParameters)) {
            return createForbidden("Bad signature")
        }

        println("Signature is valid")
        val practitionerEmail = keycloakController.getUserEmail(prescriptionRenewal.practitionerUserId) ?: return createNotFound("No practitioner email found")
        println("Found practitioner email")
        println("checkout status $checkoutStatus")
        when (checkoutStatus) {
            "ok" -> {
                prescriptionController.updatePrescriptionRenewalStatus(prescriptionRenewal, PaymentStatus.PAID)
                println("updated status to paid")
                emailController.sendPrescriptionRenewalEmail(
                    prescriptionRenewal,
                    practitionerEmail,
                    ssn,
                    firstName,
                    lastName
                )
                println("sent the email")
                prescriptionController.deletePrescriptionRenewal(prescriptionRenewal)
            }
            "pending", "delayed" -> return createAccepted()
            "fail" -> {
                return createBadRequest("Payment failed")
            }
        }

        return createOk()
    }

    @RolesAllowed(value = [UserRole.PATIENT.name])
    override fun createPrescriptionRenewal(prescriptionRenewal: PrescriptionRenewal): Response {
        val userId = loggedUserId ?: return createUnauthorized("Unauthorized")
        keycloakController.getUserEmail(prescriptionRenewal.practitionerUserId) ?: return createNotFound("No practitioner email found")

        prescriptionRenewal.successRedirectUrl ?: return createBadRequest("Success redirect URL is required")
        prescriptionRenewal.cancelRedirectUrl ?: return createBadRequest("Cancel Redirect URL is required")
        val paymentData = paymentController.initRenewPrescriptionPayment(prescriptionRenewal, userId)
        paymentData ?: return createInternalServerError("Failed to create payment")

        val newPrescriptionRenewal = prescriptionController.createPrescriptionRenewal(
            id = paymentData.id,
            stamp = paymentData.stamp,
            price = paymentData.price,
            prescriptions = prescriptionRenewal.prescriptions,
            practitionerUserId = prescriptionRenewal.practitionerUserId,
            paymentStatus = prescriptionRenewal.status,
            paymentUrl = paymentData.paymentUrl,
            transactionId = paymentData.transactionId,
            checkoutAccount = paymentData.checkoutAccount,
            userId = userId
        )

        return createOk(prescriptionRenewalTranslator.translate(newPrescriptionRenewal))
    }

    @RolesAllowed(value = [UserRole.PATIENT.name])
    override fun getPrescriptionRenewal(id: UUID): Response {
        loggedUserId ?: return createUnauthorized("Unauthorized")
        val prescriptionRenewal = prescriptionController.findPrescriptionRenewal(id)

        prescriptionRenewal ?: return createNotFound()
        return createOk(prescriptionRenewalTranslator.translate(prescriptionRenewal))
    }

    override fun ping(): Response {
        return Response.ok("pong").build()
    }
}