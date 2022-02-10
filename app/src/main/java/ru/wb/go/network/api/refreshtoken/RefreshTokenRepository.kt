package ru.wb.go.network.api.refreshtoken

import io.reactivex.Completable

interface RefreshTokenRepository {

    fun refreshAccessTokenSync()

    fun refreshAccessToken(): Completable

}
