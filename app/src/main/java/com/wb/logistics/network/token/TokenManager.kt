package com.wb.logistics.network.token

import com.wb.logistics.network.api.auth.entity.TokenEntity

interface TokenManager {
    fun saveToken(token: TokenEntity)
    fun bearerToken(): String
    fun bearerRefreshToken(): String
    fun refreshToken(): String
    fun userName(): String
    fun userCompany(): String
    fun clear()
    fun isContains(): Boolean
}