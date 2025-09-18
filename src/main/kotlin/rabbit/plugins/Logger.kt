package rabbit.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.logging.*

object Logger {
    private val logger = mutableMapOf(
        "main" to KtorSimpleLogger("rabbit.ExceptionFilter"),
        "database" to KtorSimpleLogger("rabbit.ExceptionFilter.Database"),
        "Transformation" to KtorSimpleLogger("rabbit.ExceptionFilter.Transformation"),
        "websocket" to KtorSimpleLogger("rabbit.ExceptionFilter.WebSocket"),
        "info" to KtorSimpleLogger("rabbit.info")
    )

    fun callFailed(call: ApplicationCall, cause: Throwable, prefix: String = "main") {
        logger[prefix].let {
            val channel = if (it == null) {
                val logger = this.logger["main"]!!
                logger.debug("Logger $prefix not found")
                logger
            } else
                it

            channel.warn("Request ${call.request.path()} was failed due to $cause")
            channel.debug("Stacktrace => ${cause.stackTraceToString()}")
        }
    }

    fun debug(message: Any?, prefix: String) {
        logger[prefix].let {
            val logger = if (it == null) {
                logger["main"]!!.debug("Logger $prefix not found")
                logger["main"]!!
            } else it

            logger.debug(message.toString())
        }
    }

    fun debugException(message: Any?, cause: Throwable, prefix: String) {
        logger[prefix].let {
            val logger = if (it == null) {
                logger["main"]!!.debug("Logger $prefix not found")
                logger["main"]!!
            } else it

            logger.debug(message.toString())
            logger.debug("Something failed due to $cause")
            logger.debug("Stacktrace => ${cause.stackTraceToString()}")
        }
    }
}
