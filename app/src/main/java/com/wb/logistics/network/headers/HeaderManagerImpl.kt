package com.wb.logistics.network.headers

import java.util.*

class HeaderManagerImpl(private val tokenManager: TokenManager) : HeaderManager {
    override val headerApiMap: Map<String, String>
        get() {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap[CONTENT_TYPE] = "application/json"
            headerMap[ACCEPT] = "application/json"
            headerMap[TOKEN_AUTH] = tokenManager.bearerToken
            return headerMap
        }

    companion object {
        const val CONTENT_TYPE = "Content-Type"
        const val ACCEPT = "Accept"
        const val TOKEN_AUTH = "Authorization"
    }
}