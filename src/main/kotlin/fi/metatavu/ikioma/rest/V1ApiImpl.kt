package fi.metatavu.ikioma.rest

import fi.metatavu.ikioma.controllers.EmailController
import fi.metatavu.ikioma.controllers.PaymentController
import fi.metatavu.ikioma.controllers.PrescriptionRenewalController
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
    private lateinit var logger: Logger

    override fun checkoutFinlandCancel(): Response {
        TODO("Not yet implemented")
    }

    @RolesAllowed(value = [UserRole.PATIENT.name])
    override fun checkoutFinlandSuccess(
        checkoutAccount: Int,
        checkoutAlgorithm: String,
        checkoutAmount: Int,
        checkoutStamp: String,
        checkoutReference: String,
        checkoutTransactionId: String,
        checkoutStatus: String,
        checkoutProvider: String,
        signature: String
    ): Response {
        val userId = loggedUserId ?: return createUnauthorized("Unauthorized")
        val refNo: UUID?
        try {
            refNo = UUID.fromString(checkoutReference)
        } catch (e: IllegalArgumentException) {
            return createBadRequest("Invalid reference no")
        }

        val prescriptionRenewal = prescriptionController.findPrescriptionRenewalByReference(reference = refNo)
        prescriptionRenewal ?: return createNotFound()

        if (prescriptionRenewal.stamp != UUID.fromString(checkoutStamp) || prescriptionRenewal.transactionId != checkoutTransactionId) {
            return createForbidden("Payment information does not match")
        }

        if (!paymentController.verifyPayment(signature, mapOf(
                Pair("checkout-account" ,checkoutAccount),
                Pair("checkout-algorithm", checkoutAlgorithm),
                Pair("checkout-amount", checkoutAmount),
                Pair("checkout-stamp", checkoutStamp),
                Pair("checkout-reference", checkoutReference),
                Pair("checkout-transaction-id", checkoutTransactionId),
                Pair("checkout-status", checkoutStatus),
                Pair("checkout-provider", checkoutProvider)))) {
            return createForbidden("Bad signature")
        }

        prescriptionController.updatePrescriptionRenewalStatus(prescriptionRenewal, PaymentStatus.PAID)
        //send email
       // emailController.sendPrescriptionRenewalSuccess(prescriptionRenewal, userId, "ssn")

        prescriptionController.deletePrescriptionRenewal(prescriptionRenewal)
        return createOk()
    }

    @RolesAllowed(value = [UserRole.PATIENT.name])
    override fun createPrescriptionRenewal(prescriptionRenewal: PrescriptionRenewal): Response {
        val userId = loggedUserId ?: return createUnauthorized("Unauthorized")

        val paymentData = paymentController.initRenewPrescriptionPayment(prescriptionRenewal, userId)
        paymentData ?: return createInternalServerError("Failed to create payment")

        val newPrescriptionRenewal = prescriptionController.createPrescriptionRenewal(
            id = paymentData.id,
            stamp = paymentData.stamp,
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