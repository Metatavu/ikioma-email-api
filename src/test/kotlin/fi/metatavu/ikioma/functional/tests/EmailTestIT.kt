package fi.metatavu.ikioma.functional.tests

import fi.metatavu.ikioma.email.api.client.models.Email
import fi.metatavu.ikioma.functional.resources.TestBuilder
import fi.metatavu.ikioma.integrations.test.functional.resources.KeycloakTestResource
import io.quarkus.mailer.MockMailbox
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.inject.Inject

@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class)
)
class EmailTestIT {

    @Inject
    private lateinit var mailbox: MockMailbox

    @BeforeEach
    fun init() {
        mailbox.clear()
    }

    /**
     * Tests sending emails using async email service
     */
    @Test
    fun sendSimpleEmailTest() {
        TestBuilder().use { testBuilder ->
            val email = Email(
                receiverAddress = "tero.ayramo@example.com",
                subject = "Prescription renewal request Tero",
                messageBody = "Request new prescription for Burana"
            )

            val email1 = Email(
                receiverAddress = "onni.korhonen@example.com",
                subject = "Prescription renewal request Onni",
                messageBody = "Request new prescription for Burana"
            )

            var i = 0
            while (i < 100) {
                testBuilder.onniKorhonen().emails.sendEmail(email)
                i++
            }
            Assertions.assertEquals(100, mailbox.totalMessagesSent)
            val teroMessages = mailbox.getMessagesSentTo(email.receiverAddress)
            Assertions.assertEquals(100, teroMessages.size)
            Assertions.assertEquals(email.messageBody, teroMessages[0].text)
            Assertions.assertEquals(email.subject, teroMessages[0].subject)

            var k = 0
            while (k < 500) {
                testBuilder.onniKorhonen().emails.sendEmail(email1)
                k++
            }
            Assertions.assertEquals(600, mailbox.totalMessagesSent)
            val onniMessages = mailbox.getMessagesSentTo(email1.receiverAddress)
            Assertions.assertEquals(500, onniMessages.size)
            Assertions.assertEquals(email1.messageBody, onniMessages[0].text)
            Assertions.assertEquals(email1.subject, onniMessages[0].subject)
        }
    }

    /**
     * Tests sending emails with invalid fields
     */
    @Test
    fun sendInvalidEmailTestFail() {
        TestBuilder().use { testBuilder ->
            val invalidAddress = Email(
                receiverAddress = " asd sad t@#e    ro  .ayr aa",
                subject = "Prescription renewal request",
                messageBody = "Request new prescription for Burana"
            )
            testBuilder.onniKorhonen().emails.assertSendFail(400, invalidAddress)

            val emptyFields = Email(receiverAddress = "onni.korhonen@example.com", subject = "", messageBody = "")
            testBuilder.onniKorhonen().emails.assertSendFail(400, emptyFields)
        }
    }
}