package fi.metatavu.ikioma.functional.tests

import fi.metatavu.ikioma.email.api.client.models.Email
import fi.metatavu.ikioma.functional.resources.TestBuilder
import fi.metatavu.ikioma.integrations.test.functional.resources.KeycloakTestResource
import io.quarkus.mailer.MockMailbox
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import javax.inject.Inject

@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(KeycloakTestResource::class)
)
class EmailTestIT {

    @Inject
    private lateinit var mailbox: MockMailbox

    @Test
    fun sendSimpleEmailTest() {
        TestBuilder().use { testBuilder ->

            val email = Email(
                receiverAddress = "tero.ayramo@example.com",
                subject = "Prescription renewal request",
                messageBody = "Request new prescription for Burana"
            )
            testBuilder.onniKorhonen().emails.sendEmail(email)
            Assertions.assertEquals(1, mailbox.totalMessagesSent)
            val teroMessages = mailbox.getMessagesSentTo(email.receiverAddress)
            Assertions.assertEquals(1, teroMessages.size)
            Assertions.assertEquals(email.messageBody, teroMessages[0].text)
            Assertions.assertEquals(email.subject, teroMessages[0].subject)
        }
    }
}