package ru.wb.go.network.headers

import io.reactivex.Completable

interface RefreshTokenRepository {

    fun refreshAccessTokens()

    fun refreshAccessTokensSync(): Completable

}
