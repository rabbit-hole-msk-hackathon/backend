package rabbit.modules.user.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.select
import rabbit.exceptions.BadRequestException
import org.jetbrains.exposed.sql.transactions.transaction
import rabbit.modules.auth.data.models.UserLoginModel
import rabbit.modules.user.data.dto.UserOutputDto
import rabbit.modules.user.data.dto.UserUpdateDto
import rabbit.modules.user.data.models.UserModel
import rabbit.utils.database.BaseIntEntity
import rabbit.utils.database.BaseIntEntityClass
import rabbit.utils.database.idValue
import rabbit.utils.security.bcrypt.CryptoUtil
import java.util.NoSuchElementException

class UserDao(id: EntityID<Int>): BaseIntEntity<UserOutputDto>(id, UserModel) {
    companion object : BaseIntEntityClass<UserOutputDto, UserDao>(UserModel) {
        fun checkUnique(login: String) = transaction {
            val search = UserModel.select {
                UserModel.login eq login
            }
            if (!search.empty())
                throw BadRequestException("Login must be unique")
        }

        fun new(authorName: String, init: UserDao.() -> Unit): UserDao {
            val userDao = super.new(init)
            return userDao
        }
    }

    var name by UserModel.name
    var login by UserModel.login
    var hash by UserModel.hash

    val lastLogin: Long?
        get() = try {
            val gotItem = UserLoginModel.slice(UserLoginModel.lastLogin).select { UserLoginModel.userId eq idValue }.first()
            gotItem[UserLoginModel.lastLogin]
        } catch (e: NoSuchElementException) {
            null
        }

    override fun toOutputDto(): UserOutputDto =
        UserOutputDto(idValue, name, login, null, lastLogin ?: 0L)

    private fun toOutputWithHash(): UserOutputDto =
        UserOutputDto(idValue, name, login, hash, lastLogin ?: 0L)

    private fun loadPatch(userUpdateDto: UserUpdateDto) = transaction {
        if (userUpdateDto.login != null && userUpdateDto.login != login) {
            checkUnique(userUpdateDto.login!!)
            login = userUpdateDto.login!!
        }
        if (userUpdateDto.name != null)
            name = userUpdateDto.name!!
        if (userUpdateDto.password != null)
            hash = CryptoUtil.hash(userUpdateDto.password!!)
        if (userUpdateDto.hash != null)
            hash = userUpdateDto.hash!!
    }

    fun loadAndFlush(userUpdateDto: UserUpdateDto): Boolean {
        if (userUpdateDto.password != null)
            userUpdateDto.hash =  CryptoUtil.hash(userUpdateDto.password!!)

        loadPatch(userUpdateDto)

        return flush()
    }

    fun delete(authorName: String) {
        super.delete()
    }
}