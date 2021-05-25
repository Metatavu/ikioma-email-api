package fi.metatavu.ikioma.rest.translate

import fi.metatavu.ikioma.persistence.models.PrescriptionRenewal
import java.net.URI
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * Translator class for JPA Prescription Renewal entities to REST Prescription Renewal objects
 */
@ApplicationScoped
class PrescriptionRenewalTranslator: AbstractTranslator<PrescriptionRenewal, fi.metatavu.ikioma.email.api.spec.model.PrescriptionRenewal>() {

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
        prescriptionRenewal.prescriptions = entity.prescriptions?.toList()
        prescriptionRenewal.transactionId = entity.transactionId
        prescriptionRenewal.status = enumValueOf(entity.paymentStatus.toString())
        return prescriptionRenewal
    }
}