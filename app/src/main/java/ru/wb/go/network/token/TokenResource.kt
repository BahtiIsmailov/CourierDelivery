package ru.wb.go.network.token

data class TokenResource(
    val exp: Int = 0,
    val jti: String = "",
    val iat: Int = 0,
    val iss: String = "",
    val sub: String? = "",
    val extra: Extra = Extra(),
    val refreshExpiresAt: Int = 0
)
