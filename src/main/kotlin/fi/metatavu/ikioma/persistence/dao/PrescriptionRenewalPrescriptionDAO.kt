package fi.metatavu.ikioma.persistence.dao

import fi.metatavu.ikioma.persistence.models.Prescription
import fi.metatavu.ikioma.persistence.models.PrescriptionRenewal
import fi.metatavu.ikioma.persistence.models.PrescriptionRenewalPrescription
import fi.metatavu.ikioma.persistence.models.PrescriptionRenewalPrescription_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class for managing the connection table between Prescription and PrescriptionRenewal tables
 */
@ApplicationScoped
class PrescriptionRenewalPrescriptionDAO : AbstractDAO<PrescriptionRenewalPrescription>() {

    /**
     * Lists all the links for the provided prescription renewal
     *
     * @param entity prescription renewal
     * @return list of prescription renewal connections
     */
    fun listByPrescriptionRenwal(entity: PrescriptionRenewal): List<PrescriptionRenewalPrescription> {
        val entityManager = getEntityManager()

        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<PrescriptionRenewalPrescription> = criteriaBuilder.createQuery(
            PrescriptionRenewalPrescription::class.java
        )
        val root: Root<PrescriptionRenewalPrescription> = criteria.from(PrescriptionRenewalPrescription::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(PrescriptionRenewalPrescription_.prescriptionRenewal), entity))
        return entityManager.createQuery(criteria).resultList
    }

    /**
     * Creates new connection between prescription and prescription renewal request
     *
     * @param id id
     * @param prescription prescription
     * @param createdRenewalRequest prescription renewal
     * @return created connection
     */
    fun create(
        id: UUID,
        prescription: Prescription,
        createdRenewalRequest: PrescriptionRenewal
    ): PrescriptionRenewalPrescription {
        val prescriptionRenewalPrescription = PrescriptionRenewalPrescription()
        prescriptionRenewalPrescription.id = id
        prescriptionRenewalPrescription.prescription = prescription
        prescriptionRenewalPrescription.prescriptionRenewal = createdRenewalRequest
        return persist(prescriptionRenewalPrescription)
    }

    /**
     * Lists all the links for the provided prescription
     *
     * @param prescription prescription filter
     * @return list of prescription renewal connections
     */
    fun listByPrescription(prescription: Prescription): List<PrescriptionRenewalPrescription> {
        val entityManager = getEntityManager()

        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<PrescriptionRenewalPrescription> = criteriaBuilder.createQuery(
            PrescriptionRenewalPrescription::class.java
        )
        val root: Root<PrescriptionRenewalPrescription> = criteria.from(PrescriptionRenewalPrescription::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(PrescriptionRenewalPrescription_.prescription), prescription))
        return entityManager.createQuery(criteria).resultList
    }

}