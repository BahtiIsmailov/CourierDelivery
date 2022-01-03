package ru.wb.go.network.token

import ru.wb.go.network.api.auth.entity.TokenEntity

interface TokenManager {
    fun apiVersion(): String
    fun wbUserID(): String
    fun saveToken(token: TokenEntity)
    fun bearerToken(): String
    fun bearerRefreshToken(): String
    fun refreshToken(): String
    fun userName(): String
    fun userInn(): String
//    fun userInn(inn: String)
    fun userCompany(): String
    fun userCompanyId(): String
    fun userPhone() : String
    fun clear()
    fun isContains(): Boolean
    fun resources(): List<String>
}