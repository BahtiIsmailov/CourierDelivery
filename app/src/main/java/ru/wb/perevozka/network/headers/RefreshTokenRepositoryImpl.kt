package ru.wb.perevozka.network.headers

import ru.wb.perevozka.network.api.auth.entity.TokenEntity
import ru.wb.perevozka.network.api.auth.query.RefreshTokenQuery
import ru.wb.perevozka.network.api.auth.response.RefreshResponse
import ru.wb.perevozka.network.token.TokenManager
import io.reactivex.Completable
import io.reactivex.Single


class RefreshTokenRepositoryImpl(
    private var server: RefreshTokenApi, private val tokenManager: TokenManager,
) : RefreshTokenRepository {

    override fun refreshAccessTokens() {
        saveToken(convertTokenEntity(refreshTokenResponse()))
    }

    private fun refreshTokenResponse() = server.refreshAccessTokens(tokenManager.bearerToken(),
        RefreshTokenQuery(tokenManager.refreshToken()))
        .execute().body() ?: throw NullPointerException()

    private fun convertTokenEntity(refreshResponse: RefreshResponse) =
        with(refreshResponse) {
            TokenEntity(accessToken, expiresIn, refreshToken)
        }

    private fun saveToken(tokenEntity: TokenEntity) {
        tokenManager.saveToken(tokenEntity)
    }

    override fun refreshAccessTokensSync(): Completable {
        return Completable.fromSingle(Single.fromCallable { refreshTokenResponse() }
            .map { convertTokenEntity(it) }.doOnSuccess { saveToken(it) })
    }

}