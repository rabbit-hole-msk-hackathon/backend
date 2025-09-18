package rabbit.modules.user.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserInputDto (
    val name: String,
    val login: String,
    val password: String
)