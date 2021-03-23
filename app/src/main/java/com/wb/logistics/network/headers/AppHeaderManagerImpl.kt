package com.wb.logistics.network.headers

import com.wb.logistics.app.APP_JSON
import com.wb.logistics.network.headers.HeaderManager.Companion.ACCEPT
import com.wb.logistics.network.headers.HeaderManager.Companion.CONTENT_TYPE
import com.wb.logistics.network.headers.HeaderManager.Companion.HOST
import com.wb.logistics.network.headers.HeaderManager.Companion.TOKEN_AUTH
import java.util.*

class AppHeaderManagerImpl(private val token: String, private val host: String) : HeaderManager {
    override val headerApiMap: Map<String, String>
        get() {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap[CONTENT_TYPE] = APP_JSON
            headerMap[ACCEPT] = APP_JSON
            headerMap[TOKEN_AUTH] = token
            headerMap[HOST] = host
            return headerMap
        }
}