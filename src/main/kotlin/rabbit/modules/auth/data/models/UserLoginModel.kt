package rabbit.modules.auth.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import rabbit.modules.user.data.models.UserModel
import rabbit.utils.database.BaseIntIdTable

object UserLoginModel: BaseIntIdTable() {
    val userId = reference("user_id", UserModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val lastLogin = long("last_login")
}