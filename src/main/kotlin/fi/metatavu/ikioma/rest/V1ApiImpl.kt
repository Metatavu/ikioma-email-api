package fi.metatavu.ikioma.rest

import fi.metatavu.ikioma.email.EmailController
import fi.metatavu.ikioma.email.api.api.spec.V1Api
import fi.metatavu.ikioma.email.api.spec.model.Email
import io.smallrye.mutiny.Uni
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.core.Response


/**
 * V1 API
 */
@RequestScoped
class V1ApiImpl: V1Api, AbstractApi()  {

    @Inject
    private lateinit var emailController: EmailController

    override fun createEmail(email: Email): Response {
        emailController.sendEmail(email.receiverAddress, email.subject, email.messageBody)
        return Response.accepted().build();
    }

    override fun ping(): Response {
        return Response.ok("pong").build()
    }
}