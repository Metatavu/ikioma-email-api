package fi.metatavu.ikioma.rest

import org.eclipse.microprofile.jwt.JsonWebToken
import org.slf4j.Logger
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject

@RequestScoped
abstract class AbstractApi {

    @Inject
    private lateinit var logger: Logger

    @Inject
    private lateinit var jsonWebToken: JsonWebToken

    /**
     * Returns logged user id
     *
     * @return logged user id
     */
    protected open fun loggerUserId(): UUID? {
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
}
