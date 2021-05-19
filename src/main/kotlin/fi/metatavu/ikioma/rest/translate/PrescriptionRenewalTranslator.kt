package fi.metatavu.ikioma.rest.translate

import fi.metatavu.ikioma.persistence.models.PrescriptionRenewal
import java.net.URI
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PrescriptionRenewalTranslator: AbstractTranslator<PrescriptionRenewal, fi.metatavu.ikioma.email.api.spec.model.PrescriptionRenewal>() {

    /**
     * Translates JPA exposure instances into REST exposure instances
     *
     * @param entity JPA exposure instance
     *
     * @return REST exposure instance
     */
    override fun translate(entity: PrescriptionRenewal): fi.metatavu.ikioma.email.api.spec.model.PrescriptionRenewal {
        val prescriptionRenewal = fi.metatavu.ikioma.email.api.spec.model.PrescriptionRenewal()
        prescriptionRenewal.id = entity.id
        if (entity.paymentUrl != null) {
            prescriptionRenewal.paymentUrl = URI.create(entity.paymentUrl)
        }

        prescriptionRenewal.practitionerUserId = entity.practitionerUserId
        prescriptionRenewal.prescriptions = entity.prescriptions?.toList()
        prescriptionRenewal.transactionId = entity.transactionId

        prescriptionRenewal.status = enumValueOf(entity.paymentStatus.toString())
        return prescriptionRenewal
    }
}