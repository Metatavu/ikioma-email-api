package fi.metatavu.ikioma.rest

import fi.metatavu.ikioma.email.EmailController
import fi.metatavu.ikioma.email.api.api.spec.V1Api
import fi.metatavu.ikioma.email.api.spec.model.Email
import org.apache.commons.validator.routines.EmailValidator
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
            emailController.sendEmailAsync(email.receiverAddress, email.subject, email.messageBody)!!.subscribeAsCompletionStage()!!.toCompletableFuture()!!.get(5L, TimeUnit.SECONDS)
            createAccepted()
        } catch (e: Exception) {
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