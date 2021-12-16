package fi.metatavu.ikioma.rest

import fi.metatavu.ikioma.email.api.spec.model.Error
import org.eclipse.microprofile.jwt.JsonWebToken
import org.jboss.resteasy.spi.HttpRequest
import org.slf4j.Logger
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.core.Context
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext
import kotlin.collections.HashMap

/**
 * Abstract base class for all API services
 *
 * @author Antti Leppä
 */
@RequestScoped
abstract class AbstractApi {

    @Inject
    private lateinit var logger: Logger

    @Inject
    private lateinit var jsonWebToken: JsonWebToken

    @Context
    private lateinit var securityContext: SecurityContext

    @Context
    private lateinit var request: HttpRequest

    /**
     * Returns logged user id
     *
     * @return logged user id
     */
    protected val loggedUserId: UUID?
        get() {
            if (jsonWebToken.subject != null) {
                return try {
                    UUID.fromString(jsonWebToken.subject)
                } catch (ex: IllegalArgumentException) {
                    logger.error(ex.message)
                    null
                }
            }

            return null
        }

    /**
     * Returns parameters from the current request
     *
     * @return map of checkout parameters
     */
    protected fun getCheckoutParameters(): MutableMap<String, String> {
        val sortedMap = request?.uri?.queryParameters?.filterKeys { it.startsWith("checkout-") }
        val checkoutParameters = mutableMapOf<String,String>()
        sortedMap?.forEach { (s, mutableList) -> checkoutParameters[s] = mutableList[0] }
        return checkoutParameters
    }

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

    /**
     * Constructs ok response
     *
     * @return response
     */
    protected fun createOk(): Response {
        return Response
            .status(Response.Status.OK)
            .build()
    }

    /**
     * Constructs no content response
     *
     * @return response
     */
    protected fun createAccepted(): Response {
        return Response
            .status(Response.Status.ACCEPTED)
            .build()
    }

    /**
     * Constructs no content response
     *
     * @return response
     */
    protected fun createNoContent(): Response {
        return Response
            .status(Response.Status.NO_CONTENT)
            .build()
    }

    /**
     * Constructs bad request response
     *
     * @param message message
     * @return response
     */
    protected fun createBadRequest(message: String): Response {
        return createError(Response.Status.BAD_REQUEST, message)
    }

    /**
     * Constructs not found response
     *
     * @param message message
     * @return response
     */
    protected fun createNotFound(message: String): Response {
        return createError(Response.Status.NOT_FOUND, message)
    }

    /**
     * Constructs not found response
     *
     * @return response
     */
    protected fun createNotFound(): Response {
        return Response
            .status(Response.Status.NOT_FOUND)
            .build()
    }


    /**
     * Constructs not found response
     *
     * @param message message
     * @return response
     */
    protected fun createConflict(message: String): Response {
        return createError(Response.Status.CONFLICT, message)
    }

    /**
     * Constructs not implemented response
     *
     * @param message message
     * @return response
     */
    protected fun createNotImplemented(message: String): Response {
        return createError(Response.Status.NOT_IMPLEMENTED, message)
    }

    /**
     * Constructs internal server error response
     *
     * @param message message
     * @return response
     */
    protected fun createInternalServerError(message: String): Response {
        return createError(Response.Status.INTERNAL_SERVER_ERROR, message)
    }

    /**
     * Constructs forbidden response
     *
     * @param message message
     * @return response
     */
    protected fun createForbidden(message: String): Response {
        return createError(Response.Status.FORBIDDEN, message)
    }

    /**
     * Constructs unauthorized response
     *
     * @param message message
     * @return response
     */
    protected fun createUnauthorized(message: String): Response {
        return createError(Response.Status.UNAUTHORIZED, message)
    }

    /**
     * Constructs an error response
     *
     * @param status status code
     * @param message message
     *
     * @return error response
     */
    private fun createError(status: Response.Status, message: String): Response {
        val entity = Error()

        entity.message = message
        entity.code = status.statusCode

        return Response
            .status(status)
            .entity(entity)
            .build()
    }

    companion object {
        const val NOT_FOUND_MESSAGE = "Not found"
        const val UNAUTHORIZED = "Unauthorized"
        const val FORBIDDEN = "Forbidden"
        const val MISSING_REQUEST_BODY = "Missing request body"
    }

}