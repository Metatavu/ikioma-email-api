package fi.metatavu.ikioma.email

import fi.metatavu.ikioma.persistence.models.PrescriptionRenewal
import io.quarkus.mailer.Mail
import io.quarkus.mailer.Mailer
import io.quarkus.mailer.reactive.ReactiveMailer
import io.smallrye.mutiny.Uni
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for sending emails
 */
@ApplicationScoped
class EmailController {

    @Inject
    private lateinit var mailer: Mailer

    @Inject
    private lateinit var reactiveMailer: ReactiveMailer

    @Inject
    private lateinit var logger: org.slf4j.Logger

    /**
     * Sends email using blocking mailer service
     *
     * @param to target address
     * @param subject email subject
     * @param textData email text
     */
    fun sendEmail(to: String, subject: String, textData: String) {
        mailer.send(Mail.withText(to, subject, textData))
    }

    /**
     * Sends email using non blocking mailer
     *
     * @param to receiver address
     * @param subject subject
     * @param textData email text
     * @return sending status
     */
    fun sendEmailAsync(to: String, subject: String, textData: String): Uni<Void> {
        return reactiveMailer.send(Mail.withText(to, subject, textData))
    }

    /**
     * Sends prescription renewal request to the practitioner
     *
     * @param prescriptionRenewal prescription renewal object
     * @param email practitioner email
     * @param ssn patient ssn
     * @param firstName patient first name
     * @param lastName patient last name
     */
    fun sendPrescriptionRenewalEmail(
        prescriptionRenewal: PrescriptionRenewal,
        email: String,
        ssn: String,
        firstName: String,
        lastName: String
    ) {
        sendEmail(
            email,
            "SEC reseptinuusintapyyntö",
            "Henkilö $firstName $lastName ($ssn) pyytää reseptin uusintaa resepteille ${prescriptionRenewal.prescriptions.joinToString(", ")}"
        )
    }
}