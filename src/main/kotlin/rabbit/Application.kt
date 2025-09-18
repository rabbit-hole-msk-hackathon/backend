package rabbit

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import rabbit.conf.AppConf
import rabbit.conf.DatabaseInitializer
import rabbit.modules.auth.controller.AuthController
import rabbit.modules.auth.data.models.UserLoginModel
import rabbit.modules.auth.service.AuthService
import rabbit.modules.user.controller.UserController
import rabbit.modules.user.data.models.UserModel
import rabbit.modules.user.service.*
import rabbit.plugins.*
import rabbit.utils.database.DatabaseConnector
import rabbit.utils.kodein.bindSingleton
import rabbit.utils.kodein.kodeinApplication
import rabbit.utils.websockets.WebSocketRegister

fun main() {
    embeddedServer(Netty, port = AppConf.server.port, host = AppConf.server.host, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureCORS()
    configureMonitoring()
    configureSerialization()
    configureSockets()
    configureExceptionFilter()

    kodeinApplication {
        bindSingleton { AuthService(it) }
        bindSingleton { UserService(it) }
        bindSingleton { WebSocketRegister(it) }


        bindSingleton { AuthController(it) }
        bindSingleton { UserController(it) }
    }

    DatabaseConnector(
        UserModel, UserLoginModel,
    ) {
        DatabaseInitializer.initUsers()
        commit()
    }
}
