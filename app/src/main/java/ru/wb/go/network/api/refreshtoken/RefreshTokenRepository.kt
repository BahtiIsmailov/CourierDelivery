package ru.wb.go.network.api.refreshtoken

import io.reactivex.Single

interface RefreshTokenRepository {

    fun doRefreshToken():RefreshResult

}
