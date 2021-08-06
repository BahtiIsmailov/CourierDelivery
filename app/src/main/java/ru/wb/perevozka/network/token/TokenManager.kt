package ru.wb.perevozka.network.token

import ru.wb.perevozka.network.api.auth.entity.TokenEntity

interface TokenManager {
    fun apiVersion(): String
    fun saveToken(token: TokenEntity)
    fun bearerToken(): String
    fun bearerRefreshToken(): String
    fun refreshToken(): String
    fun userName(): String
    fun userCompany(): String
    fun clear()
    fun isContains(): Boolean
}