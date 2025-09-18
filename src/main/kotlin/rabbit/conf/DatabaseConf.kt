package rabbit.conf

data class DatabaseConf (
    val url: String,
    val driver: String,
    val user: String,
    val password: String
)