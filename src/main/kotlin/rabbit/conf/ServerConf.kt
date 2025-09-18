package rabbit.conf

data class ServerConf(
    val host: String,
    val port: Int,
    val fileLocation: String,
)
