package ru.wb.perevozka.network.api.auth

import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.perevozka.network.api.auth.entity.TokenEntity
import ru.wb.perevozka.network.api.auth.entity.UserInfoEntity
import ru.wb.perevozka.network.api.auth.query.AuthBySmsOrPasswordQuery
import ru.wb.perevozka.network.api.auth.query.ChangePasswordBySmsCodeQuery
import ru.wb.perevozka.network.api.auth.query.PasswordCheckQuery
import ru.wb.perevozka.network.api.auth.query.RefreshTokenQuery
import ru.wb.perevozka.network.api.auth.response.CheckExistPhoneResponse
import ru.wb.perevozka.network.api.auth.response.RemainingAttemptsResponse
import ru.wb.perevozka.network.api.auth.response.StatisticsResponse
import ru.wb.perevozka.network.token.TokenManager
import ru.wb.perevozka.network.token.UserManager

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
            .toCompletable()
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

    override fun checkExistAndSavePhone(phone: String): Single<CheckExistPhoneResponse> {
        return authApi.checkExistPhone(tokenManager.apiVersion(), phone)
            .doOnSuccess { userManager.savePhone(phone) }
    }

    override fun sendTmpPassword(phone: String): Single<RemainingAttemptsResponse> {
        return authApi.sendTmpPassword(tokenManager.apiVersion(), phone)
    }

    override fun changePasswordBySmsCode(
        phone: String,
        password: String,
        tmpPassword: String,
    ): Completable {
        val requestBody = ChangePasswordBySmsCodeQuery(password, tmpPassword)
        return authApi.changePasswordBySmsCode(tokenManager.apiVersion(), phone, requestBody)
    }

    override fun passwordCheck(
        phone: String,
        tmpPassword: String,
    ): Completable {
        return authApi.passwordCheck(
            tokenManager.apiVersion(),
            phone,
            PasswordCheckQuery(tmpPassword)
        )
    }

    override fun statistics(): Single<StatisticsResponse> {
        return authApi.statistics(tokenManager.apiVersion())
    }

    override fun userInfo(): Single<UserInfoEntity> {
        return Single.just(UserInfoEntity(tokenManager.userName(), tokenManager.userCompany()))
    }

    override fun clearToken() {
        tokenManager.clear()
        userManager.clear()
    }

    override fun userPhone(): String {
        return userManager.phone()
    }

}