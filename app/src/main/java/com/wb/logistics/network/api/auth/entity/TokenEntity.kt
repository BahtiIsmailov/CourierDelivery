package com.wb.logistics.network.api.auth.entity

data class TokenEntity(
    val accessToken: String,
    val expiresIn: Int,
    val refreshToken: String
)