package fi.metatavu.ikioma.functional.tests

import io.quarkus.mailer.MockMailbox
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import javax.inject.Inject

/**
 * JVM tests for prescription renewals API
 */
@QuarkusTest
class JvmPrescriptionRenewalsTestsIT: BasePrescriptionRenewalsTestsIT() {

    @Inject
    private lateinit var mailbox: MockMailbox

    @BeforeEach
    fun init() {
        mailbox.clear()
    }
    /**
     * Asserts mails for prescriptionRenewal test.
     *
     * @param korhonenEmail email for korhonen
     */
    override fun prescriptionRenewalAssertMails(korhonenEmail: String) {
        val practitionerMessages = mailbox.getMessagesSentTo(korhonenEmail)

        Assertions.assertNotNull(practitionerMessages)
        Assertions.assertEquals(1, practitionerMessages.size)
        Assertions.assertEquals("SEC reseptinuusintapyyntö", practitionerMessages[0].subject)
        Assertions.assertTrue(practitionerMessages[0].text.startsWith("Henkilö Tero Testi Äyrämö (010170-999R) pyytää reseptin uusintaa resepteille "))
        Assertions.assertTrue(practitionerMessages[0].text.contains("Even more Burana"))
        Assertions.assertTrue(practitionerMessages[0].text.contains("All the Burana"))
    }

}