package fi.metatavu.ikioma.rest

import fi.metatavu.ikioma.email.EmailController
import java.util.concurrent.CompletionStage
import javax.inject.Inject
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Response

@Path("/async")
class AsyncEndpoint {

    @Inject
    private lateinit var emailController: EmailController

    @POST
    fun sendMail(
        @QueryParam(value = "receiver") receiver: String,
        @QueryParam(value = "subject") subject: String,
        @QueryParam(value = "body") body: String
    ): CompletionStage<Response?>? {
        return emailController.sendEmailAsync(receiver, subject, body)?.subscribeAsCompletionStage()
            ?.thenApply { Response.accepted().build() }
    }
}