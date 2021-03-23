package com.wb.logistics.network.headers

import com.wb.logistics.app.APP_JSON
import com.wb.logistics.network.headers.HeaderManager.Companion.ACCEPT
import com.wb.logistics.network.headers.HeaderManager.Companion.CONTENT_TYPE
import java.util.*

class AuthHeaderManagerImpl : HeaderManager {
    override val headerApiMap: Map<String, String>
        get() {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap[CONTENT_TYPE] = APP_JSON
            headerMap[ACCEPT] = APP_JSON
            return headerMap
        }
}