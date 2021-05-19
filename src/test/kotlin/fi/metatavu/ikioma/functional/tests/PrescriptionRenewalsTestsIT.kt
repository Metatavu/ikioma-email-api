package fi.metatavu.ikioma.functional.tests

import fi.metatavu.ikioma.email.api.client.models.PaymentStatus
import fi.metatavu.ikioma.email.api.client.models.PrescriptionRenewal
import fi.metatavu.ikioma.functional.resources.MysqlResource
import fi.metatavu.ikioma.functional.resources.TestBuilder
import fi.metatavu.ikioma.integrations.test.functional.resources.KeycloakTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests Payments API
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class),
    QuarkusTestResource(MysqlResource::class)
)
class PrescriptionRenewalsTestsIT {

    /**
     * Tests sending emails with invalid address
     */
    @Test
    fun prescriptionRenewal() {
        TestBuilder().use { builder ->
            val practitionerId = UUID.randomUUID()

            val createdPrescription = builder.teroAyramo().prescriptionRenewals.create(
                PrescriptionRenewal(
                    status = PaymentStatus.nOTPAID,
                    practitionerUserId = practitionerId,
                    prescriptions = arrayOf("Burana", "Even more Burana", "All the burana")
                )
            )

            Assertions.assertNotNull(createdPrescription.transactionId)
            Assertions.assertNotNull(createdPrescription.paymentUrl)
            Assertions.assertNotNull(createdPrescription.id)

            val foundRenewalRequest = builder.teroAyramo().prescriptionRenewals.find(createdPrescription.id!!)
            Assertions.assertEquals(createdPrescription.transactionId, foundRenewalRequest.transactionId)
            Assertions.assertEquals(createdPrescription.practitionerUserId, foundRenewalRequest.practitionerUserId)
            Assertions.assertEquals(createdPrescription.status, foundRenewalRequest.status)
        }
    }
}