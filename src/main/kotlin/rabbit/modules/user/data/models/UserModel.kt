package rabbit.modules.user.data.models

import rabbit.utils.database.BaseIntIdTable

object UserModel: BaseIntIdTable() {
    val name = text("name")
    val login = text("login")
    val hash = text("hash")
}