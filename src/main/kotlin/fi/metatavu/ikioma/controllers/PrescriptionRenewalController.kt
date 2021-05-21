package fi.metatavu.ikioma.controllers

import fi.metatavu.ikioma.email.api.spec.model.PaymentStatus
import fi.metatavu.ikioma.persistence.dao.PrescriptionRenewalDAO
import fi.metatavu.ikioma.persistence.models.PrescriptionRenewal
import liquibase.pro.packaged.S
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
     * @param id reference number
     * @param prescriptions list of prescriptions
     * @param practitionerUserId practitioner user id
     * @param paymentStatus payment status
     * @param paymentUrl payment url
     * @param stamp stamp
     * @param transactionId transaction id
     * @param userId user id
     * @return saved prescription renewal object
     */
    fun createPrescriptionRenewal(
        id: UUID,
        prescriptions: List<String>,
        practitionerUserId: UUID,
        paymentStatus: PaymentStatus,
        paymentUrl: String,
        stamp: UUID,
        checkoutAccount: Int,
        transactionId: String?,
        userId: UUID
    ): PrescriptionRenewal {
        return prescriptionRenewalDAO.create(
            id = id,
            prescriptions = prescriptions,
            paymentUrl = paymentUrl,
            transactionId = transactionId,
            practitionerUserId = practitionerUserId,
            paymentStatus = paymentStatus,
            stamp = stamp,
            checkoutAccount = checkoutAccount,
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
     * Finds prescription renewal request by reference id
     *
     * @param reference payment reference id
     * @return found renewal request
     */
    fun findPrescriptionRenewalByReference(reference: UUID): PrescriptionRenewal? {
        return prescriptionRenewalDAO.findByTransactionId(reference)
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