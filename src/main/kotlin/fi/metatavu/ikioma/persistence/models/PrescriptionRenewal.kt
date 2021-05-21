package fi.metatavu.ikioma.persistence.models

import fi.metatavu.ikioma.email.api.spec.model.PaymentStatus
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*

/**
 * JPA entity for prescription renewal requests
 *
 * @property id id which is represented by reference number of initiated payment
 * @property paymentUrl payment url from the checkout payment service
 * @property prescriptions list of prescriptions requested
 * @property transactionId transaction id
 * @property practitionerUserId practitioner user id
 * @property stamp unique identifier of the payment
 * @property checkoutAccount merchant id
 * @property paymentStatus payment status
 * @property createdAt created at
 * @property modifiedAt modified at
 * @property creatorId creator id
 * @property lastModifierId last modifier id
 */
@Entity
class PrescriptionRenewal {

    @Id
    var id: UUID? = null

    @Column
    var paymentUrl: String? = null

    @ElementCollection
    @CollectionTable(name = "Prescriptions", joinColumns = [JoinColumn(name = "ID")])
    @Column(name = "prescriptionname", nullable = false)
    lateinit var prescriptions: Collection<String>

    @Column
    var transactionId: String? = null

    @Column(nullable = false)
    lateinit var practitionerUserId: UUID

    @Column(nullable = false)
    lateinit var stamp: UUID

    @Column(nullable = false)
    var checkoutAccount: Int? = null

    @Column(nullable = false)
    lateinit var paymentStatus: PaymentStatus

    @Column(nullable = false)
    lateinit var createdAt: OffsetDateTime

    @Column(nullable = false)
    lateinit var modifiedAt: OffsetDateTime

    @Column(nullable = false)
    lateinit var creatorId: UUID

    @Column(nullable = false)
    lateinit var lastModifierId: UUID

    /**
     * JPA pre-persist event handler
     */
    @PrePersist
    fun onCreate() {
        createdAt = OffsetDateTime.now()
        modifiedAt = OffsetDateTime.now()
    }

    /**
     * JPA pre-update event handler
     */
    @PreUpdate
    fun onUpdate() {
        modifiedAt = OffsetDateTime.now()
    }
}