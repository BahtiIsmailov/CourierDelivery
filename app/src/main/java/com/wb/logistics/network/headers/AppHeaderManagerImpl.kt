package com.wb.logistics.network.headers

import com.wb.logistics.network.headers.HeaderManager.Companion.ACCEPT
import com.wb.logistics.network.headers.HeaderManager.Companion.CONTENT_TYPE
import com.wb.logistics.network.headers.HeaderManager.Companion.TOKEN_AUTH
import java.util.*

class AppHeaderManagerImpl(private val token: String) : HeaderManager {
    override val headerApiMap: Map<String, String>
        get() {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap[CONTENT_TYPE] = "application/json"
            headerMap[ACCEPT] = "application/json"
            headerMap[TOKEN_AUTH] = token
            return headerMap
        }
}