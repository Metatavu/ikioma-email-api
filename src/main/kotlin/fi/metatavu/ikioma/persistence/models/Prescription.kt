package fi.metatavu.ikioma.persistence.models

import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

/**
 * JPA entity describing single medicine prescription
 *
 * @property id unique id
 * @property prescriptionName presciption name
 */
@Entity
class Prescription {

    @Id
    var id: UUID? = null

    @Column
    var prescriptionName: String? = null
}