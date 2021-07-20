package fi.metatavu.ikioma.functional.tests

import io.quarkus.mailer.MockMailbox
import io.quarkus.test.junit.DisabledOnNativeImage
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.BeforeEach
import javax.inject.Inject

/**
 * JVM tests for prescription renewals API
 */
@QuarkusTest
@DisabledOnNativeImage
class JvmPrescriptionRenewalsTestsIT: BasePrescriptionRenewalsTestsIT() {

    @Inject
    private lateinit var mailbox: MockMailbox

    @BeforeEach
    fun init() {
        mailbox.clear()
    }

    override fun getMailbox(): MockMailbox? {
        return mailbox
    }

}