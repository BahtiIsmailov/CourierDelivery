package com.wb.logistics.network.headers

import com.wb.logistics.network.api.auth.entity.TokenEntity
import com.wb.logistics.network.api.auth.query.RefreshTokenQuery
import com.wb.logistics.network.token.TokenManager


class RefreshTokenRepositoryImpl(
    private var server: RefreshTokenApi, private val tokenManager: TokenManager,
) : RefreshTokenRepository {

    override fun refreshAccessTokens() {
        val refreshResponse = server.refreshAccessTokens(tokenManager.bearerToken(),
            RefreshTokenQuery(tokenManager.refreshToken()))
            .execute().body() ?: throw NullPointerException()
        val tokenEntity = with(refreshResponse) {
            TokenEntity(accessToken, expiresIn, refreshToken)
        }
        tokenManager.saveToken(tokenEntity)
    }

}