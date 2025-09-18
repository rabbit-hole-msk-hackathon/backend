package rabbit.modules.auth.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import rabbit.modules.auth.data.dto.AuthInputDto
import rabbit.modules.auth.service.AuthService
import rabbit.utils.kodein.KodeinController

class AuthController(override val di: DI) : KodeinController() {
    private val authService: AuthService by instance()

    override fun Routing.registerRoutes() {
        route("auth") {
            post {
                val authInput = call.receive<AuthInputDto>()

                call.respond(authService.auth(authInput))
            }
            authenticate("refresh") {
                post("refresh") {
                    val refreshTokenDto = getRefresh(call)

                    call.respond(authService.refreshUser(refreshTokenDto))
                }
            }
            authenticate("default") {
                route("authorized") {
                    get {
                        call.respond(authService.getAuthorized(call.getAuthorized()))
                    }
                }
            }
        }
    }

}