package ru.wb.go.network.headers

import ru.wb.go.network.headers.HeaderManager.Companion.HOST

class AppDemoHeaderManagerImpl(private val host: String) : HeaderManager {
    override val headerApiMap: Map<String, String>
        get() {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap[HOST] = host
            return headerMap
        }
}