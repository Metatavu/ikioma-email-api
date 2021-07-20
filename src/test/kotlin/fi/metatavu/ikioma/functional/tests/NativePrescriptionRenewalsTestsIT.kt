package fi.metatavu.ikioma.functional.tests

import io.quarkus.test.junit.NativeImageTest

/**
 * Native tests for prescription renewals API
 */
@NativeImageTest
class NativePrescriptionRenewalsTestsIT: BasePrescriptionRenewalsTestsIT() {

    /**
     * Asserts mails for prescriptionRenewal test.
     *
     * Currently native tests have no way of testing mail sent so this assertion is omitted
     *
     * @param korhonenEmail email for korhonen
     */
    override fun prescriptionRenewalAssertMails(korhonenEmail: String) {

    }

}