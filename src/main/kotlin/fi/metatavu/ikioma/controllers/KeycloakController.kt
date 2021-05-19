package fi.metatavu.ikioma.controllers

import org.eclipse.microprofile.config.inject.ConfigProperty
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.admin.client.resource.UsersResource
import org.keycloak.representations.idm.RoleRepresentation
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Class for keycloak controller
 *
 * @author Jari Nykänen
 */
@ApplicationScoped
class KeycloakController {

    @Inject
    @ConfigProperty(name = "ikioma.keycloak.url")
    private lateinit var authServerUrl: String

    @Inject
    @ConfigProperty(name = "ikioma.keycloak.realm")
    private lateinit var realm: String

    @Inject
    @ConfigProperty(name = "ikioma.keycloak.api-admin.secret")
    private lateinit var clientSecret: String

    @Inject
    @ConfigProperty(name = "ikioma.keycloak.api-admin.client")
    private lateinit var clientId: String

    @Inject
    @ConfigProperty(name = "ikioma.keycloak.api-admin.user")
    private lateinit var apiAdminUser: String

    @Inject
    @ConfigProperty(name = "ikioma.keycloak.api-admin.password")
    private lateinit var apiAdminPassword: String

    /**
     * Gets users SSN from keycloak
     *
     * @param userId user keycloak ID
     * @return user SSN if user and SSN attribute is found or null
     */
    fun getUserSSN(userId: UUID): String? {
        val userAttributes = getUserAttributes(userId) ?: return null
        val userSSNAttribute = userAttributes["SSN"] ?: return null
        return userSSNAttribute[0]
    }

    /**
     * Gets user email from keycloak
     *
     * @param userId UUID keycloak ID
     * @return email
     */
    fun getUserEmail(userId: UUID): String? {
        val userResource = getUserResource(userId) ?: return null
        val userRepresentation = userResource.toRepresentation() ?: return null
        return userRepresentation.email
    }

    /**
     * Gets user first name from keycloak
     *
     * @param userId UUID keycloak ID
     * @return first name
     */
    fun getFirstName(userId: UUID): String? {
        val userResource = getUserResource(userId) ?: return null
        val userRepresentation = userResource.toRepresentation() ?: return null
        return userRepresentation.firstName
    }

    /**
     * Gets user last name from keycloak
     *
     * @param userId UUID keycloak ID
     * @return last name
     */
    fun getLastName(userId: UUID): String? {
        val userResource = getUserResource(userId) ?: return null
        val userRepresentation = userResource.toRepresentation() ?: return null
        return userRepresentation.lastName
    }
    /**
     * Gets user attributes from keycloak
     *
     * @param userId user ID to search
     * @return map of user attributes
     */
    fun getUserAttributes(userId: UUID): Map<String, List<String>>? {
        val userResource = getUserResource(userId) ?: return null
        val userRepresentation = userResource.toRepresentation() ?: return null
        return userRepresentation.attributes
    }

    /**
     * Gets user resources object from keycloak
     *
     * @param userId UUID
     * @return found UserResource or null
     */
    private fun getUserResource(userId: UUID): UserResource? {
        val keycloakClient = getKeycloakClient()
        val foundRealm = keycloakClient.realm(realm)  ?: return null
        val users: UsersResource = foundRealm.users()
        return users[userId.toString()] ?: return null
    }

    /**
    * Constructs a Keycloak client
    *
    * @return Keycloak client
    */
    private fun getKeycloakClient(): Keycloak {

        return KeycloakBuilder.builder()
          .serverUrl(authServerUrl)
          .realm(realm)
          .username(apiAdminUser)
          .password(apiAdminPassword)
          .clientId(clientId)
          .clientSecret(clientSecret)
          .build()
    }

}