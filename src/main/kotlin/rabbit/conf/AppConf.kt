package rabbit.conf

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*

object AppConf {
    private val mainConfig: ApplicationConfig = HoconApplicationConfig(ConfigFactory.load().getConfig("application"))
    private val jwtConfig: ApplicationConfig = mainConfig.config("jwt")
    private val databaseConfig: ApplicationConfig = mainConfig.config("database")
    private val serverConfig: ApplicationConfig = mainConfig.config("server")

    private fun ApplicationConfig.getString(name: String): String = this.property(name).getString()
    private fun ApplicationConfig.getInt(name: String): Int = this.getString(name).toInt()

    val zoneOffset: Int = serverConfig.getInt("zoneOffset")

    val isDebug: Boolean = mainConfig.getString("debug") == "true"

    val jwt = JwtConf(
        domain = jwtConfig.getString("domain"),
        secret = jwtConfig.getString("secret"),
        expirationTime = jwtConfig.config("expiration").getInt("seconds"),
        refreshExpirationTime = jwtConfig.config("refreshExpiration").getInt("seconds"),
        mobileExpirationTime = jwtConfig.config("mobile").config("accessExpiration").getInt("seconds"),
        mobileAuthExpirationTime = jwtConfig.config("mobile").config("authExpiration").getInt("seconds")
    )

    val database = DatabaseConf(
        url = databaseConfig.getString("url"),
        driver = databaseConfig.getString("driver"),
        user = databaseConfig.getString("user"),
        password = databaseConfig.getString("password")
    )

    val server = ServerConf(
        host = serverConfig.getString("host"),
        port = serverConfig.getInt("port"),
        fileLocation = serverConfig.getString("file-location")
    )
}