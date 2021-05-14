package fi.metatavu.ikioma.rest

import fi.metatavu.ikioma.email.EmailController
import fi.metatavu.ikioma.email.api.api.spec.V1Api
import fi.metatavu.ikioma.email.api.spec.model.Email
import org.apache.commons.validator.routines.EmailValidator
import org.slf4j.Logger
import java.util.concurrent.TimeUnit
import javax.annotation.security.RolesAllowed
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.core.Response


/**
 * V1 API
 */
@RequestScoped
class V1ApiImpl : V1Api, AbstractApi() {

    @Inject
    private lateinit var emailController: EmailController

    @Inject
    private lateinit var logger: Logger

    @RolesAllowed(value = [ UserRole.PATIENT.name ])
    override fun createEmail(email: Email): Response {
        loggedUserId ?: return createUnauthorized("Unauthorized")
        if (!EmailValidator.getInstance().isValid(email.receiverAddress)) {
            return createBadRequest("Invalid email address")
        }

        if (email.messageBody.isBlank()) {
            return createBadRequest("Email message is blank")
        }

        if (email.subject.isBlank()){
            return createBadRequest("Email subject is blank")
        }

        return try {
            emailController
                .sendEmailAsync(
                    to = email.receiverAddress,
                    subject = email.subject,
                    textData = email.messageBody
                )
                .subscribeAsCompletionStage()
                .toCompletableFuture()
                .get(1L, TimeUnit.MINUTES)

            createAccepted()
        } catch (e: Exception) {
            logger.error("Email sending failed", e)

            when (e) {
                is IllegalArgumentException -> createBadRequest("Invalid email")
                else -> createInternalServerError("unknown error")
            }

        }
    }

    override fun ping(): Response {
        return Response.ok("pong").build()
    }
}