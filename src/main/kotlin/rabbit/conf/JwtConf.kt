package rabbit.conf

data class JwtConf (
    val domain: String,
    val secret: String,
    val expirationTime: Int,
    val refreshExpirationTime: Int,
    val mobileExpirationTime: Int,
    val mobileAuthExpirationTime: Int
)