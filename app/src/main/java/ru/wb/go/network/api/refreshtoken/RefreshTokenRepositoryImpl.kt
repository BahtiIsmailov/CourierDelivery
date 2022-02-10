package ru.wb.go.network.api.refreshtoken

import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.go.network.api.auth.entity.TokenEntity
import ru.wb.go.network.api.auth.query.RefreshTokenQuery
import ru.wb.go.network.api.auth.response.RefreshResponse
import ru.wb.go.network.exceptions.RefreshAccessTokenException
import ru.wb.go.network.token.TokenManager

class RefreshTokenRepositoryImpl(
    private var server: RefreshTokenApi,
    private val tokenManager: TokenManager
) : RefreshTokenRepository {

    override fun refreshAccessTokenSync() {
        saveToken(convertTokenEntity(refreshTokenResponse()))
    }

    private fun refreshTokenResponse(): RefreshResponse {
        val response = server.refreshAccessTokens(
            tokenManager.bearerToken(),
            RefreshTokenQuery(tokenManager.refreshToken())
        ).execute()
        if (response.isSuccessful) {
            return response.body() ?: throw NullPointerException()
        } else {
            throw RefreshAccessTokenException(response.errorBody()?.string() ?: "")
        }
    }

    private fun convertTokenEntity(refreshResponse: RefreshResponse) =
        with(refreshResponse) {
            TokenEntity(accessToken, expiresIn, refreshToken)
        }

    private fun saveToken(tokenEntity: TokenEntity) {
        tokenManager.saveToken(tokenEntity)
    }

    override fun refreshAccessToken(): Completable {
        return Completable.fromSingle(Single.fromCallable { refreshTokenResponse() }
            .map { convertTokenEntity(it) }.doOnSuccess { saveToken(it) })
    }

}