package ru.wb.go.network.api.refreshtoken

import io.reactivex.Single

interface RefreshTokenRepository {

     suspend fun doRefreshToken():RefreshResult

}
