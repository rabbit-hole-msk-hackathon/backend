package rabbit.utils.websockets.dto

import io.ktor.websocket.*
import rabbit.modules.auth.data.dto.AuthorizedUser
import rabbit.utils.websockets.ConnectionsRegister
import rabbit.utils.websockets.RoomsRegister

data class RequestHandlerInput (
    val request: WebSocketRequestDto,
    val authorizedUser: AuthorizedUser,
    val socketSession: DefaultWebSocketSession,
    val actualConnections: ConnectionsRegister,
    val roomsRegister: RoomsRegister
)