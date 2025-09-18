package rabbit.exceptions

import kotlinx.serialization.Serializable

@Serializable
data class InternalServerException (
    override val message: String
): BaseException(500, "Internal server error", message)