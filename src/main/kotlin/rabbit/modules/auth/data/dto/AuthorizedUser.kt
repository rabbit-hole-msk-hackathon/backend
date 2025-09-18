package rabbit.modules.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthorizedUser (
    val id: Int,
)