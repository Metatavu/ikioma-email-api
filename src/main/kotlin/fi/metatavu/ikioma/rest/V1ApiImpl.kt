package fi.metatavu.ikioma.rest

import fi.metatavu.ikioma.email.EmailController
import fi.metatavu.ikioma.email.api.api.spec.V1Api
import fi.metatavu.ikioma.email.api.spec.model.Email
import org.apache.commons.validator.routines.EmailValidator
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
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

    override fun createEmail(email: Email): Response {
        loggedUserId ?: return createUnauthorized("Unauthorized")
        if (!EmailValidator.getInstance().isValid(email.receiverAddress)) {
            return createBadRequest("Invalid email address")
        }

        if (email.messageBody.isBlank() || email.subject.isBlank()) {
            return createBadRequest("Empty fields")
        }

        return try {
            emailController.sendEmailAsync(email.receiverAddress, email.subject, email.messageBody)!!.subscribeAsCompletionStage()!!.toCompletableFuture()!!.get(5L, TimeUnit.SECONDS)
            createOk()
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