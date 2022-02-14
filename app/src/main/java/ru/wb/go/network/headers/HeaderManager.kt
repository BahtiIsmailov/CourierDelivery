package ru.wb.go.network.headers

interface HeaderManager {

    val headerApiMap: Map<String, String>

    companion object {
        const val CONTENT_TYPE = "Content-Type"
        const val TOKEN_AUTH = "Authorization"
        const val HOST = "Host"
        const val X_COORDINATES = "X-Coordinates"
        const val X_MOBILE_VERSION = "X-Mobile-Version"
    }
}