package com.wb.logistics.network.api.auth

import com.wb.logistics.network.api.auth.entity.TokenEntity
import com.wb.logistics.network.api.auth.query.AuthByPhoneOrPasswordQuery
import com.wb.logistics.network.api.auth.query.ChangePasswordBySmsCodeQuery
import com.wb.logistics.network.api.auth.query.PasswordCheckQuery
import com.wb.logistics.network.api.auth.response.CheckExistPhoneResponse
import com.wb.logistics.network.api.auth.response.RemainingAttemptsResponse
import com.wb.logistics.network.api.auth.response.StatisticsResponse
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.token.TokenManager
import io.reactivex.Completable
import io.reactivex.Single

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val tokenManager: TokenManager,
) : AuthRepository {

    override fun authByPhoneOrPassword(
        password: String, phone: String, useSMS: Boolean,
    ): Completable {
        val requestBody = AuthByPhoneOrPasswordQuery(phone, password, useSMS)
        val auth = authApi.authByPhoneOrPassword(requestBody)
            .map { TokenEntity(it.accessToken, it.expiresIn, it.refreshToken) }
            .doOnSuccess { saveToken(it) }
        return Completable.fromSingle(auth)
    }

    private fun saveToken(tokenEntity: TokenEntity) {
        tokenManager.saveToken(tokenEntity)
    }

    override fun checkExistPhone(phone: String): Single<CheckExistPhoneResponse> {
        return authApi.checkExistPhone(phone)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun sendTmpPassword(phone: String): Single<RemainingAttemptsResponse> {
        return authApi.sendTmpPassword(phone)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun changePasswordBySmsCode(
        phone: String,
        password: String,
        tmpPassword: String,
    ): Completable {
        val requestBody = ChangePasswordBySmsCodeQuery(password, tmpPassword)
        return authApi.changePasswordBySmsCode(
            phone, requestBody
        ).compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun passwordCheck(
        phone: String,
        tmpPassword: String,
    ): Completable {
        return authApi.passwordCheck(phone, PasswordCheckQuery(tmpPassword))
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun statistics(): Single<StatisticsResponse> {
        return authApi.statistics().compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun userInfo(): Single<Pair<String, String>> {
        return Single.just(Pair(tokenManager.userName(), tokenManager.userCompany()))
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun clearToken() {
        tokenManager.clear()
    }

}