package rabbit.conf

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import rabbit.modules.user.data.models.UserModel
import rabbit.utils.security.bcrypt.CryptoUtil

object DatabaseInitializer {
    fun initUsers() {
        if (!UserModel.selectAll().empty())
            return
        UserModel.insert {
            it[id] = 1
            it[name] = "Admin User"
            it[login] = "admin"
            it[hash] = CryptoUtil.hash("admin")
        }
    }
}