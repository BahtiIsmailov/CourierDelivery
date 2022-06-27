package ru.wb.go.network.headers

import ru.wb.go.network.headers.HeaderManager.Companion.TOKEN_AUTH
import ru.wb.go.network.token.TokenManager

class RefreshTokenHeaderManagerImpl(private val tokenManager: TokenManager) : HeaderManager {
    override val headerApiMap: Map<String, String>
        get() {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap[TOKEN_AUTH] = tokenManager.bearerToken()
            return headerMap
        }
}