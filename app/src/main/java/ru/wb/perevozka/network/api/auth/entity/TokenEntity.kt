package ru.wb.perevozka.network.api.auth.entity

data class TokenEntity(
    val accessToken: String,
    val expiresIn: Int,
    val refreshToken: String
)