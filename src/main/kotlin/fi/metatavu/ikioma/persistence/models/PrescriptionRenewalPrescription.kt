package fi.metatavu.ikioma.persistence.models

import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

/**
 * Entity for describing connection between prescription renewals and prescriptions
 *
 * @property id unique id
 * @property prescription prescription
 * @property prescriptionRenewal prescription renewal
 */
@Entity
class PrescriptionRenewalPrescription {

    @Id
    @Column(nullable = false)
    var id: UUID? = null

    @ManyToOne
    var prescription: Prescription? = null

    @ManyToOne
    var prescriptionRenewal: PrescriptionRenewal? = null
}