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
    @Column(name = "Prescription")
    var prescriptions: Collection<String>? = null

    @Column
    var transactionId: String? = null

    @Column(nullable = false)
    var practitionerUserId: UUID? = null

    @Column(nullable = false)
    var stamp: UUID? = null

    @Column(nullable = false)
    var checkoutAccount: Int? = null

    @Column(nullable = false)
    var paymentStatus: PaymentStatus? = null

    @Column(nullable = false)
    var createdAt: OffsetDateTime? = null

    @Column(nullable = false)
    var modifiedAt: OffsetDateTime? = null

    @Column(nullable = false)
    var creatorId: UUID? = null

    @Column(nullable = false)
    var lastModifierId: UUID? = null

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