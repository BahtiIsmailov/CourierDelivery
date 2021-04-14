package com.wb.logistics.network.headers

import com.wb.logistics.network.headers.HeaderManager.Companion.HOST
import com.wb.logistics.network.headers.HeaderManager.Companion.TOKEN_AUTH
import com.wb.logistics.network.token.TokenManager
import java.util.*

class AppHeaderManagerImpl(private val tokenManager: TokenManager, private val host: String) : HeaderManager {
    override val headerApiMap: Map<String, String>
        get() {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap[TOKEN_AUTH] = tokenManager.bearerToken()
            headerMap[HOST] = host
            return headerMap
        }
}