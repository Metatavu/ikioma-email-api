package fi.metatavu.ikioma.rest

import fi.metatavu.ikioma.email.EmailController
import fi.metatavu.ikioma.email.api.api.spec.V1Api
import fi.metatavu.ikioma.email.api.spec.model.Email
import javax.inject.Inject
import javax.ws.rs.core.Response

/**
 * V1 API
 */
class V1ApiImpl: V1Api, AbstractApi() {

    @Inject
    private lateinit var emailController: EmailController

    override fun createEmail(email: Email?): Response {
        TODO("Not yet implemented")
    }

    override fun ping(): Response {
        return createOk("pong")
    }
}