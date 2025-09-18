package rabbit.modules.user.controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import rabbit.modules.user.data.dto.UserFilterDto
import rabbit.modules.user.data.dto.UserOutputDto
import rabbit.modules.user.service.*
import rabbit.utils.kodein.KodeinController

class UserController(override val di: DI) : KodeinController() {
    private val userService: UserService by instance()

    override fun Routing.registerRoutes() {
        authenticate("default") {
            route("user") {
                post("all") {
                    val userFilterDto = call.receive<UserFilterDto>()

                    call.respond<List<UserOutputDto>>(userService.getByFilter(userFilterDto))
                }
                get {
                    val authorizedUser = call.getAuthorized()
                    call.respond<UserOutputDto>(userService.getOne(authorizedUser.id))
                }
            }
        }
    }
}