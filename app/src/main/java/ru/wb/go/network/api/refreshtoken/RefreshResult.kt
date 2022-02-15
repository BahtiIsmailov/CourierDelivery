package ru.wb.go.network.api.refreshtoken

import ru.wb.go.network.api.auth.response.RefreshResponse

sealed class RefreshResult {
    object Success : RefreshResult()
    object Failed : RefreshResult()
    object TokenInvalid : RefreshResult()
}
