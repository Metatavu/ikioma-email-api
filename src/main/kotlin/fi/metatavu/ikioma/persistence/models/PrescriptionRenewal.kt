package fi.metatavu.ikioma.persistence.models

import fi.metatavu.ikioma.email.api.spec.model.PaymentStatus
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*


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

    @Column
    var practitionerUserId: UUID? = null

    @Column
    var stamp: UUID? = null

    @Column
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