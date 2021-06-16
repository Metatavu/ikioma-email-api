package fi.metatavu.ikioma.prescriptions

import fi.metatavu.ikioma.persistence.dao.PrescriptionDAO
import fi.metatavu.ikioma.persistence.models.Prescription
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for managing prescriptions
 *
 * @property prescriptionDAO PrescriptionDAO
 */
@ApplicationScoped
class PrescriptionController {

    @Inject
    private lateinit var prescriptionDAO: PrescriptionDAO

    /**
     * Creates prescription
     *
     * @param prescriptionName name
     * @return created prescription
     */
    fun createPrescription(prescriptionName: String): Prescription {
        return prescriptionDAO.create(UUID.randomUUID(), prescriptionName)
    }

    /**
     * Finds prescription by name
     *
     * @param prescriptionName name to filter by
     * @return found prescription or null
     */
    fun findByName(prescriptionName: String): Prescription? {
        return prescriptionDAO.findByName(prescriptionName)
    }
}