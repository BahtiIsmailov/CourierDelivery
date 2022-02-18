package ru.wb.go.network.api.refreshtoken

import okhttp3.Response

sealed class RefreshResult {
    object Success : RefreshResult()
    data class Failed(val ex: Throwable) : RefreshResult()
    object TokenInvalid : RefreshResult()
    object TimeOut : RefreshResult()
}
