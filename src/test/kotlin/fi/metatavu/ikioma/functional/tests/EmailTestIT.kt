package fi.metatavu.ikioma.functional.tests

import fi.metatavu.ikioma.email.api.client.models.Email
import fi.metatavu.ikioma.functional.resources.TestBuilder
import fi.metatavu.ikioma.integrations.test.functional.resources.KeycloakTestResource
import io.quarkus.mailer.MockMailbox
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
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
     * Tests sending emails using blocking email service
     */
    @Test
    fun sendSimpleEmailTest() {
        TestBuilder().use { testBuilder ->
            val email = Email(
                receiverAddress = "tero.ayramo@example.com",
                subject = "Prescription renewal request",
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
        }
    }

    @Test
    fun sendAsyncEmailTest() {
        val sub = "subject"
        val body = "body"
        val to = "receiver"
        val params1 = mapOf(
            Pair(sub, "prescription renewal 1"),
            Pair(body, "burana 1"),
            Pair(to, "tero.ayramo@example.com")
        )
        var i = 0
        while (i < 100) {
            given()
                .contentType("application/json")
                .queryParams(params1)
                .`when`()
                .post("/async")
                .then()
                .statusCode(202)
            i++
        }

        Assertions.assertEquals(100, mailbox.totalMessagesSent)
        val teroMessages = mailbox.getMessagesSentTo("tero.ayramo@example.com")
        Assertions.assertEquals(100, teroMessages.size)
    }
}