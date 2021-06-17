package fi.metatavu.ikioma.persistence.dao

import fi.metatavu.ikioma.persistence.models.Prescription
import fi.metatavu.ikioma.persistence.models.Prescription_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class to manage Presciption table
 */
@ApplicationScoped
class PrescriptionDAO: AbstractDAO<Prescription>() {

    /**
     * Finds prescription by its name
     *
     * @param prescriptionName prescription name
     * @return found prescription
     */
    fun findByName(prescriptionName: String): Prescription? {
        val entityManager = getEntityManager()

        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<Prescription> = criteriaBuilder.createQuery(
            Prescription::class.java
        )
        val root: Root<Prescription> = criteria.from(Prescription::class.java)
        criteria.select(root)
        criteria.where(
            criteriaBuilder.equal(root.get(Prescription_.prescriptionName), prescriptionName)
        )

        return getSingleResult(entityManager.createQuery(criteria))
    }

    /**
     * Creates new prescription
     *
     * @param id id
     * @param prescriptionName prescription name
     * @return created prescription
     */
    fun create(id: UUID, prescriptionName: String?): Prescription {
        val prescription = Prescription()
        prescription.id = id
        prescription.prescriptionName = prescriptionName
        return persist(prescription)
    }
}