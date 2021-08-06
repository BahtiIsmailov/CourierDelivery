package ru.wb.perevozka.network.token

data class TokenResource(
    val exp: Int,
    val jti: String,
    val iat: Int,
    val iss: String,
    val sub: String,
    val extra: Extra,
    val refreshExpiresAt: Int
)
