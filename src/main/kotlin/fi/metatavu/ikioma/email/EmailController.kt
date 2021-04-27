package fi.metatavu.ikioma.email

import io.quarkus.mailer.Mail
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
    private lateinit var reactiveMailer: ReactiveMailer

    fun sendEmail(to: String, subject: String, textData: String) {
        val mail = Mail.withText(to, subject, textData)
        val stage: Uni<Void> = reactiveMailer.send(mail)
    }
}