package rabbit.utils.kodein

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import org.kodein.di.DIAware
import org.kodein.di.instance
import rabbit.exceptions.BadRequestException
import rabbit.modules.auth.data.dto.RefreshTokenDto
import rabbit.modules.auth.data.dto.AuthorizedUser
import rabbit.utils.security.jwt.JwtUtil

/**
 * A [KodeinAware] base class for Controllers handling routes.
 * It allows to easily get dependencies, and offers some useful extensions.
 */
@Suppress("KDocUnresolvedReference")
abstract class KodeinController : DIAware {
    /**
     * Injected dependency with the current [Application].
     */
    val application: Application by instance()

    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    abstract fun Routing.registerRoutes()

    fun ApplicationCall.getAuthorized(): AuthorizedUser {
        val principal = principal<JWTPrincipal>()!!
        return JwtUtil.decodeAccessToken(principal)
    }

    fun Parameters.getInt(name: String, errorMsg: String): Int {
        val param = this[name] ?: throw BadRequestException(errorMsg)
        return try {
            param.toInt()
        } catch (e: NumberFormatException) {
            throw BadRequestException(errorMsg)
        }
    }

    fun getRefresh(call: ApplicationCall): RefreshTokenDto {
        val principal = call.principal<JWTPrincipal>()!!
        return JwtUtil.decodeRefreshToken(principal)
    }
}