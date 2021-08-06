package ru.wb.perevozka.network.headers

import ru.wb.perevozka.app.APP_JSON
import ru.wb.perevozka.network.headers.HeaderManager.Companion.CONTENT_TYPE
import java.util.*

class AuthHeaderManagerImpl : HeaderManager {
    override val headerApiMap: Map<String, String>
        get() {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap[CONTENT_TYPE] = APP_JSON
            return headerMap
        }
}