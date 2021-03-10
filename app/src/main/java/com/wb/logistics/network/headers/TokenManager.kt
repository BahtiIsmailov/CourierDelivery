package com.wb.logistics.network.headers

interface TokenManager {
    fun saveApiToken(token: String)
    val bearerToken: String
    fun clear()
}