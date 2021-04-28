package fi.metatavu.ikioma.email

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

    fun sendEmail(to: String, subject: String, textData: String) {
        mailer.send(Mail.withText(to, subject, textData))
    }
}