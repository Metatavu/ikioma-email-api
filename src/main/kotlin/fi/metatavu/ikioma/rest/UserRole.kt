package fi.metatavu.ikioma.rest

/**
 * Sealed class for user keycloak role
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 */
sealed class UserRole(val role: String) {

    object PATIENT: UserRole("patient") {
        const val name = "patient"
    }

    object USER: UserRole("user") {
        const val name = "user"
    }

}