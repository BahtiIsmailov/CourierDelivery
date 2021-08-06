package ru.wb.perevozka.network.headers

import io.reactivex.Completable

interface RefreshTokenRepository {

    fun refreshAccessTokens()

    fun refreshAccessTokensSync()  : Completable

}
