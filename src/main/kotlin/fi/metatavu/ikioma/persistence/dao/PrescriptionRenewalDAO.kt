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
     * @param id UUID
     * @param transactionId String?
     * @param prescriptions List<String>
     * @param practitionerUserId UUID?
     * @param paymentStatus PaymentStatus?
     * @param paymentUrl String?
     * @param creatorId UUID
     * @param lastModifierId UUID
     * @return PrescriptionRenewal
     */
    fun create(
        id: UUID,
        transactionId: String?,
        prescriptions: List<String>,
        practitionerUserId: UUID?,
        paymentStatus: PaymentStatus?,
        paymentUrl: String?,
        creatorId: UUID,
        lastModifierId: UUID
    ): PrescriptionRenewal {
        val prescriptionRenewal = PrescriptionRenewal()
        prescriptionRenewal.id = id
        prescriptionRenewal.prescriptions = prescriptions
        prescriptionRenewal.transactionId = transactionId
        prescriptionRenewal.paymentStatus = paymentStatus
        prescriptionRenewal.paymentUrl = paymentUrl
        prescriptionRenewal.practitionerUserId = practitionerUserId
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
     * @param transactionId payment transaction id
     * @return prescription renewal
     */
    fun findByTransactionId(transactionId: String): PrescriptionRenewal? {
        val entityManager = getEntityManager()

        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<PrescriptionRenewal> = criteriaBuilder.createQuery(
            PrescriptionRenewal::class.java
        )
        val root: Root<PrescriptionRenewal> = criteria.from(PrescriptionRenewal::class.java)
        criteria.select(root)
        criteria.where(
            criteriaBuilder.equal(root.get(PrescriptionRenewal_.transactionId), transactionId)
        )

        return getSingleResult(entityManager.createQuery(criteria))
    }
}