package rabbit.modules.user.service

import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import rabbit.exceptions.BadRequestException
import rabbit.modules.auth.data.dto.AuthorizedUser
import rabbit.modules.auth.data.models.UserLoginModel
import rabbit.modules.user.data.dao.UserDao
import rabbit.modules.user.data.dao.UserDao.Companion.createLikeCond
import rabbit.modules.user.data.dto.*
import rabbit.modules.user.data.models.UserModel
import rabbit.utils.database.idValue
import rabbit.utils.kodein.KodeinService
import rabbit.utils.security.bcrypt.CryptoUtil

class UserService(di: DI) : KodeinService(di) {

    fun createUser(authorizedUser: AuthorizedUser, createUserDto: UserInputDto): UserOutputDto = transaction {

        UserDao.checkUnique(createUserDto.login)

        val authorName = UserDao[authorizedUser.id].login

        val userDao = UserDao.new(authorName) {
            name = createUserDto.name
            login = createUserDto.login
            hash = CryptoUtil.hash(createUserDto.password)
        }

        commit()

        UserOutputDto(
            id = userDao.idValue,
            name = userDao.name,
            login = userDao.login,
            lastLogin = 0,
        )
    }

    fun removeUser(authorizedUser: AuthorizedUser, userId: Int): UserRemoveOutputDto = transaction {
        val authorName = UserDao[authorizedUser.id].login
        val userDao = UserDao[userId]

        userDao.delete(authorName)

        commit()

        UserRemoveOutputDto(userId, "success")
    }

    fun updateUser(authorizedUser: AuthorizedUser, userId: Int, userUpdateDto: UserUpdateDto): UserOutputDto = transaction {
        val authorName = UserDao[authorizedUser.id].login
        val userDao = UserDao[userId]

        if (userUpdateDto.hash != null)
            throw BadRequestException("Bad request")

        userDao.loadAndFlush(userUpdateDto)
        commit()

        userDao.toOutputDto()
    }

    fun getOne(userId: Int): UserOutputDto = transaction { UserDao[userId].toOutputDto() }

    fun getByFilter(userFilterDto: UserFilterDto): List<UserOutputDto> = transaction {
        UserModel
            .join(UserLoginModel, JoinType.LEFT)
            .select {
                createLikeCond(userFilterDto.login, UserModel.id neq 0, UserModel.login) and
                createLikeCond(userFilterDto.name, UserModel.id neq 0, UserModel.name)
            }
            .orderBy(UserModel.login to SortOrder.ASC)
            .map {
                val lastLogin = if (it[UserLoginModel.lastLogin] == null)
                    0L
                else
                    it[UserLoginModel.lastLogin]
                UserOutputDto(
                    id = it[UserModel.id].value,
                    name = it[UserModel.name],
                    login = it[UserModel.login],
                    lastLogin = lastLogin
                )
            }
    }
}