package fi.metatavu.ikioma.rest

import javax.enterprise.context.RequestScoped
import javax.ws.rs.core.Response

@RequestScoped
abstract class AbstractApi {

    /**
     * Constructs ok response
     *
     * @param entity payload
     * @return response
     */
    protected fun createOk(entity: Any?): Response {
        return Response
            .status(Response.Status.OK)
            .entity(entity)
            .build()
    }
}
