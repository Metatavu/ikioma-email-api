package fi.metatavu.ikioma.controllers

import fi.metatavu.ikioma.email.api.spec.model.PaymentStatus
import fi.metatavu.ikioma.persistence.dao.PrescriptionRenewalDAO
import fi.metatavu.ikioma.persistence.models.PrescriptionRenewal
import java.net.URI
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller fpr prescription renewals
 */
@ApplicationScoped
class PrescriptionRenewalController {

    @Inject
    private lateinit var prescriptionRenewalDAO: PrescriptionRenewalDAO

    /**
     * Creates new prescription renewal database entry
     *
     * @param prescriptions list of prescriptions
     * @param practitionerUserId practitioner user id
     * @param paymentStatus payment status
     * @param paymentUrl payment url
     * @param transactionId transaction id
     * @param userId user id
     * @return saved prescription renewal object
     */
    fun createPrescriptionRenewal(
        prescriptions: List<String>,
        practitionerUserId: UUID,
        paymentStatus: PaymentStatus?,
        paymentUrl: URI?,
        transactionId: String?,
        userId: UUID
    ): PrescriptionRenewal {
        return prescriptionRenewalDAO.create(
            id = UUID.randomUUID(),
            prescriptions = prescriptions,
            paymentUrl = paymentUrl.toString(),
            transactionId = transactionId,
            practitionerUserId = practitionerUserId,
            paymentStatus = paymentStatus,
            creatorId = userId,
            lastModifierId = userId
        )
    }

    /**
     * Finds prescription renewal by its id
     *
     * @param id renewal id
     * @return found entity
     * */
    fun findPrescriptionRenewal(id: UUID): PrescriptionRenewal? {
        return prescriptionRenewalDAO.findById(id)
    }

    /**
     * Finds pr renewal request by transaction id
     *
     * @param transactionId payment transaction id
     * @return found renewal request
     */
    fun findPrescriptionRenewalByTransactionId(transactionId: String): PrescriptionRenewal? {
        return prescriptionRenewalDAO.findByTransactionId(transactionId)
    }

    /**
     * Updates the payment status of prescription renewal
     *
     * @param prescriptionRenewal prescription renewal
     * @param paymentStatus new payment status
     * @return updated prescription renewal
     */
    fun updatePrescriptionRenewalStatus(
        prescriptionRenewal: PrescriptionRenewal,
        paymentStatus: PaymentStatus
    ): PrescriptionRenewal {
        return prescriptionRenewalDAO.updatePaymentStatus(prescriptionRenewal, paymentStatus)
    }

    /**
     * Removes prescription renewal from the database
     *
     * @param prescriptionRenewal renewal request to delete
     */
    fun deletePrescriptionRenewal(prescriptionRenewal: PrescriptionRenewal) {
        return prescriptionRenewalDAO.delete(prescriptionRenewal)
    }
}