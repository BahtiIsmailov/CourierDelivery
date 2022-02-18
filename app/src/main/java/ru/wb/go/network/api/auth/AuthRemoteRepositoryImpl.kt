package ru.wb.go.network.api.auth

import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.go.network.api.auth.entity.TokenEntity
import ru.wb.go.network.api.auth.entity.UserInfoEntity
import ru.wb.go.network.api.auth.query.AuthBySmsOrPasswordQuery
import ru.wb.go.network.api.auth.response.StatisticsResponse
import ru.wb.go.network.token.TokenManager
import ru.wb.go.network.token.UserManager
import ru.wb.go.utils.managers.SettingsManager

class AuthRemoteRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
    private val settingsManager: SettingsManager,
) : AuthRemoteRepository {

    override fun auth(
        password: String, phone: String, useSMS: Boolean,
    ): Completable {
        val requestBody = AuthBySmsOrPasswordQuery(phone, password, useSMS)
        val auth = authApi.auth(tokenManager.apiVersion(), requestBody)
            .map { TokenEntity(it.accessToken, it.expiresIn, it.refreshToken) }
            .doOnSuccess { saveToken(it) }
            .doOnSuccess { turnOffDemo() }
        return Completable.fromSingle(auth)
    }

    override fun couriersExistAndSavePhone(phone: String): Completable {
        return authApi.couriersAuth(tokenManager.apiVersion(), phone)
            .doOnSuccess { userManager.savePhone(phone) }
            .ignoreElement()
    }

    private fun saveToken(tokenEntity: TokenEntity) {
        tokenManager.saveToken(tokenEntity)
    }

    private fun turnOffDemo() {
        tokenManager.turnOffDemo()
    }

    override fun statistics(): Single<StatisticsResponse> {
        return authApi.statistics(tokenManager.apiVersion())
    }

    override fun userInfo(): Single<UserInfoEntity> {
        return Single.just(UserInfoEntity(tokenManager.userName(), tokenManager.userCompany()))
    }

    override fun clearCurrentUser() {
        userManager.clearAll()
        settingsManager.resetSettings()
    }

    override fun userPhone(): String {
        return userManager.phone()
    }

}