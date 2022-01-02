package ru.wb.go.network.api.auth

import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.go.network.api.auth.entity.TokenEntity
import ru.wb.go.network.api.auth.entity.UserInfoEntity
import ru.wb.go.network.api.auth.query.AuthBySmsOrPasswordQuery
import ru.wb.go.network.api.auth.query.RefreshTokenQuery
import ru.wb.go.network.api.auth.response.StatisticsResponse
import ru.wb.go.network.token.TokenManager
import ru.wb.go.network.token.UserManager

class AuthRemoteRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
) : AuthRemoteRepository {

    override fun auth(
        password: String, phone: String, useSMS: Boolean,
    ): Completable {
        val requestBody = AuthBySmsOrPasswordQuery(phone, password, useSMS)
        val auth = authApi.auth(tokenManager.apiVersion(), requestBody)
            .map { TokenEntity(it.accessToken, it.expiresIn, it.refreshToken) }
            .doOnSuccess { saveToken(it) }
        return Completable.fromSingle(auth)
    }

    override fun couriersExistAndSavePhone(phone: String): Completable {
        return authApi.couriersAuth(tokenManager.apiVersion(), phone)
            .doOnSuccess { userManager.savePhone(phone) }
            .ignoreElement()
    }

    override fun refreshToken(): Completable {
        val refreshToken = authApi.refreshToken(
            tokenManager.apiVersion(),
            RefreshTokenQuery(tokenManager.refreshToken())
        )
            .map { TokenEntity(it.accessToken, it.expiresIn, it.refreshToken) }
            .doOnSuccess { saveToken(it) }
        return Completable.fromSingle(refreshToken)
    }

    private fun saveToken(tokenEntity: TokenEntity) {
        tokenManager.saveToken(tokenEntity)
    }

    override fun statistics(): Single<StatisticsResponse> {
        return authApi.statistics(tokenManager.apiVersion())
    }

    override fun userInfo(): Single<UserInfoEntity> {
        return Single.just(UserInfoEntity(tokenManager.userName(), tokenManager.userCompany()))
    }

    override fun clearToken() {
        tokenManager.clear()
        userManager.clearAll()
    }

    override fun userPhone(): String {
        return userManager.phone()
    }

}