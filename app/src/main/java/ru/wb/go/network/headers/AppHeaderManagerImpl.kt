package ru.wb.go.network.headers

import ru.wb.go.network.headers.HeaderManager.Companion.HOST
import ru.wb.go.network.headers.HeaderManager.Companion.TOKEN_AUTH
import ru.wb.go.network.token.TokenManager
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