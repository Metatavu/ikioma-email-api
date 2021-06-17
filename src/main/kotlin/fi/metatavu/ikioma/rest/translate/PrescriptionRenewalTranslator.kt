package fi.metatavu.ikioma.rest.translate

import fi.metatavu.ikioma.persistence.dao.PrescriptionRenewalPrescriptionDAO
import fi.metatavu.ikioma.persistence.models.PrescriptionRenewal
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator class for JPA Prescription Renewal entities to REST Prescription Renewal objects
 */
@ApplicationScoped
class PrescriptionRenewalTranslator: AbstractTranslator<PrescriptionRenewal, fi.metatavu.ikioma.email.api.spec.model.PrescriptionRenewal>() {

    @Inject
    private lateinit var prescriptionRenewalPrescriptionDAO: PrescriptionRenewalPrescriptionDAO

    /**
     * Translates JPA prescription renewal into REST prescription renewal
     *
     * @param entity JPA prescription renewal
     * @return REST prescription renewal
     */
    override fun translate(entity: PrescriptionRenewal): fi.metatavu.ikioma.email.api.spec.model.PrescriptionRenewal {
        val prescriptionRenewal = fi.metatavu.ikioma.email.api.spec.model.PrescriptionRenewal()
        prescriptionRenewal.id = entity.id
        if (entity.paymentUrl != null) {
            prescriptionRenewal.paymentUrl = entity.paymentUrl
        }

        prescriptionRenewal.stamp = entity.stamp
        prescriptionRenewal.checkoutAccount = entity.checkoutAccount
        prescriptionRenewal.practitionerUserId = entity.practitionerUserId
        val prescriptionsFiltered = prescriptionRenewalPrescriptionDAO.listByPrescriptionRenwal(entity)
        prescriptionRenewal.prescriptions = prescriptionsFiltered.map { it.prescription?.prescriptionName }
        prescriptionRenewal.price = entity.price
        prescriptionRenewal.transactionId = entity.transactionId
        prescriptionRenewal.status = enumValueOf(entity.paymentStatus.toString())
        return prescriptionRenewal
    }
}