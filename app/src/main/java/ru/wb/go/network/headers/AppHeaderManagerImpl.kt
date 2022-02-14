package ru.wb.go.network.headers

import ru.wb.go.network.headers.HeaderManager.Companion.HOST
import ru.wb.go.network.headers.HeaderManager.Companion.TOKEN_AUTH
import ru.wb.go.network.headers.HeaderManager.Companion.X_COORDINATES
import ru.wb.go.network.headers.HeaderManager.Companion.X_MOBILE_VERSION
import ru.wb.go.network.token.TokenManager
import ru.wb.go.utils.managers.DeviceManager

class AppHeaderManagerImpl(
    private val tokenManager: TokenManager,
    private val deviceManager: DeviceManager,
    private val host: String
) : HeaderManager {
    override val headerApiMap: Map<String, String>
        get() {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap[TOKEN_AUTH] = tokenManager.bearerToken()
            headerMap[HOST] = host
            headerMap[X_COORDINATES] = deviceManager.lastLocation()
            headerMap[X_MOBILE_VERSION] = deviceManager.appVersion
            return headerMap
        }
}