package fi.metatavu.ikioma.persistence.dao

import fi.metatavu.ikioma.email.api.spec.model.PaymentStatus
import fi.metatavu.ikioma.persistence.models.PrescriptionRenewal
import fi.metatavu.ikioma.persistence.models.PrescriptionRenewal_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class for prescription renewals
 */
@ApplicationScoped
class PrescriptionRenewalDAO : AbstractDAO<PrescriptionRenewal>() {

    /**
     * Creates new prescription renewal object
     *
     * @param id reference id
     * @param transactionId transaction id
     * @param prescriptions prescriptions list
     * @param practitionerUserId practitioner user id
     * @param paymentStatus payment status
     * @param paymentUrl payment url provided by checkout finland
     * @param stamp unique payment id
     * @param price price
     * @param creatorId user id
     * @param lastModifierId user id
     * @return PrescriptionRenewal
     */
    fun create(
        id: UUID,
        transactionId: String?,
        prescriptions: List<String>,
        practitionerUserId: UUID,
        paymentStatus: PaymentStatus,
        paymentUrl: String,
        stamp: UUID,
        price: Int,
        checkoutAccount: Int,
        creatorId: UUID,
        lastModifierId: UUID
    ): PrescriptionRenewal {
        val prescriptionRenewal = PrescriptionRenewal()
        prescriptionRenewal.id = id
        prescriptionRenewal.transactionId = transactionId
        prescriptionRenewal.prescriptions = prescriptions
        prescriptionRenewal.practitionerUserId = practitionerUserId
        prescriptionRenewal.paymentStatus = paymentStatus
        prescriptionRenewal.paymentUrl = paymentUrl
        prescriptionRenewal.stamp = stamp
        prescriptionRenewal.price = price
        prescriptionRenewal.checkoutAccount = checkoutAccount
        prescriptionRenewal.creatorId = creatorId
        prescriptionRenewal.lastModifierId = lastModifierId

        return persist(prescriptionRenewal)
    }

    /**
     * Updates payment status field
     *
     * @param prescriptionRenewal presecription renewal request to update
     * @param paymentStatus new pament status
     * @return updated entity
     */
    fun updatePaymentStatus(
        prescriptionRenewal: PrescriptionRenewal,
        paymentStatus: PaymentStatus
    ): PrescriptionRenewal {
        prescriptionRenewal.paymentStatus = paymentStatus
        return persist(prescriptionRenewal)
    }

    /**
     * Finds for prescription renewal request by its transaction id
     *
     * @param reference payment transaction id
     * @return prescription renewal
     */
    fun findByTransactionId(reference: UUID): PrescriptionRenewal? {
        val entityManager = getEntityManager()

        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<PrescriptionRenewal> = criteriaBuilder.createQuery(
            PrescriptionRenewal::class.java
        )
        val root: Root<PrescriptionRenewal> = criteria.from(PrescriptionRenewal::class.java)
        criteria.select(root)
        criteria.where(
            criteriaBuilder.equal(root.get(PrescriptionRenewal_.id), reference)
        )

        return getSingleResult(entityManager.createQuery(criteria))
    }
}