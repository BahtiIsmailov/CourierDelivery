package com.wb.logistics.network.api.auth

import com.wb.logistics.network.api.auth.entity.TokenEntity
import com.wb.logistics.network.api.auth.query.AuthByPhoneOrPasswordQuery
import com.wb.logistics.network.api.auth.query.ChangePasswordBySmsCodeQuery
import com.wb.logistics.network.api.auth.query.PasswordCheckQuery
import com.wb.logistics.network.api.auth.response.CheckExistPhoneResponse
import com.wb.logistics.network.api.auth.response.RemainingAttemptsResponse
import com.wb.logistics.network.api.auth.response.StatisticsResponse
import com.wb.logistics.network.token.TokenManager
import com.wb.logistics.network.token.UserManager
import io.reactivex.Completable
import io.reactivex.Single

class AuthRemoteRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
) : AuthRemoteRepository {

    override fun authByPhoneOrPassword(
        password: String, phone: String, useSMS: Boolean,
    ): Completable {
        val requestBody = AuthByPhoneOrPasswordQuery(phone, password, useSMS)
        val auth = authApi.authByPhoneOrPassword(tokenManager.apiVersion(), requestBody)
            .map { TokenEntity(it.accessToken, it.expiresIn, it.refreshToken) }
            .doOnSuccess { saveToken(it) }
        return Completable.fromSingle(auth)
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
        return authApi.passwordCheck(tokenManager.apiVersion(),
            phone,
            PasswordCheckQuery(tmpPassword))
    }

    override fun statistics(): Single<StatisticsResponse> {
        return authApi.statistics(tokenManager.apiVersion())
    }

    override fun userInfo(): Single<Pair<String, String>> {
        return Single.just(Pair(tokenManager.userName(), tokenManager.userCompany()))
    }

    override fun clearToken() {
        tokenManager.clear()
        userManager.clear()
    }

    override fun userPhone(): String {
        return userManager.phone()
    }

}