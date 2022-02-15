package ru.wb.go.network.api.refreshtoken

import io.reactivex.Completable

interface RefreshTokenRepository {

    fun refreshAccessToken(): Completable

    fun doRefreshToken():RefreshResult

}
