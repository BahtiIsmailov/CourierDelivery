package com.wb.logistics.network.headers

import io.reactivex.Completable

interface RefreshTokenRepository {

    fun refreshAccessTokens()

    fun refreshAccessTokensSync()  : Completable

}
