package fi.metatavu.ikioma.prescriptions

import fi.metatavu.ikioma.email.api.spec.model.PaymentStatus
import fi.metatavu.ikioma.persistence.dao.PrescriptionDAO
import fi.metatavu.ikioma.persistence.dao.PrescriptionRenewalDAO
import fi.metatavu.ikioma.persistence.dao.PrescriptionRenewalPrescriptionDAO
import fi.metatavu.ikioma.persistence.models.Prescription
import fi.metatavu.ikioma.persistence.models.PrescriptionRenewal
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for prescription renewals
 */
@ApplicationScoped
class PrescriptionRenewalController {

    @Inject
    private lateinit var prescriptionRenewalDAO: PrescriptionRenewalDAO

    @Inject
    private lateinit var prescriptionDAO: PrescriptionDAO

    @Inject
    private lateinit var prescriptionRenewalPrescriptionDAO: PrescriptionRenewalPrescriptionDAO

    /**
     * Creates new prescription renewal database entry, creates the connection between prescription renewal and prescription description
     *
     * @param id reference number
     * @param prescriptions list of prescriptions
     * @param practitionerUserId practitioner user id
     * @param paymentStatus payment status
     * @param paymentUrl payment url
     * @param stamp stamp
     * @param price total price
     * @param transactionId transaction id
     * @param userId user id
     * @return saved prescription renewal object
     */
    fun createPrescriptionRenewal(
        id: UUID,
        prescriptions: List<Prescription>,
        practitionerUserId: UUID,
        paymentStatus: PaymentStatus,
        paymentUrl: String,
        stamp: UUID,
        price: Int,
        checkoutAccount: Int,
        transactionId: String?,
        userId: UUID
    ): PrescriptionRenewal {
        val createdRenewalRequest = prescriptionRenewalDAO.create(
            id = id,
            paymentUrl = paymentUrl,
            transactionId = transactionId,
            practitionerUserId = practitionerUserId,
            paymentStatus = paymentStatus,
            stamp = stamp,
            price = price,
            checkoutAccount = checkoutAccount,
            creatorId = userId,
            lastModifierId = userId
        )

        for (prescription in prescriptions) {
            prescriptionRenewalPrescriptionDAO.create(UUID.randomUUID(), prescription, createdRenewalRequest)
        }

        return createdRenewalRequest
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
     * Removes prescription renewal from the database, its link to the prescriptions and prescriptions if they are not
     * attached to any other renewal requests
     *
     * @param prescriptionRenewal renewal request to delete
     */
    fun deletePrescriptionRenewal(prescriptionRenewal: PrescriptionRenewal) {
        val prescriptionsRenewalsLinks = prescriptionRenewalPrescriptionDAO.listByPrescriptionRenwal(prescriptionRenewal)
        for (prescriptionRenewalLink in prescriptionsRenewalsLinks) {
            prescriptionRenewalPrescriptionDAO.delete(prescriptionRenewalLink)
            val prescription = prescriptionRenewalLink.prescription
            if (prescription != null && prescriptionRenewalPrescriptionDAO.listByPrescription(prescription).isEmpty()) {
                prescriptionDAO.delete(prescription)
            }
        }

        return prescriptionRenewalDAO.delete(prescriptionRenewal)
    }
}