package com.wb.logistics.network.headers

interface HeaderManager {
    val headerApiMap: Map<String, String>

    companion object {
        const val CONTENT_TYPE = "Content-Type"
        const val TOKEN_AUTH = "Authorization"
        const val HOST = "Host"
    }
}