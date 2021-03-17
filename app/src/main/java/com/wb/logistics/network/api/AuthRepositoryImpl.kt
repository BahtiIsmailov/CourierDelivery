package com.wb.logistics.network.api

import com.wb.logistics.network.api.query.AuthByPhoneOrPasswordQuery
import com.wb.logistics.network.api.query.ChangePasswordBySmsCodeQuery
import com.wb.logistics.network.api.query.PasswordCheckQuery
import com.wb.logistics.network.api.response.AuthResponse
import com.wb.logistics.network.api.response.CheckExistPhoneResponse
import com.wb.logistics.network.api.response.RemainingAttemptsResponse
import com.wb.logistics.network.api.response.StatisticsResponse
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Completable
import io.reactivex.Single

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val rxSchedulerFactory: RxSchedulerFactory
) : AuthRepository {

    override fun authByPhoneOrPassword(
        password: String, phone: String, useSMS: Boolean
    ): Completable {
        val requestBody = AuthByPhoneOrPasswordQuery(phone, password, useSMS)
        val auth = authApi.authByPhoneOrPassword(requestBody).doOnSuccess { saveToken(it) }
        return Completable.fromSingle(auth).compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun saveToken(authResponse: AuthResponse) {
        // TODO:16.03.2021 реализовать хранилище токена
    }

    override fun checkExistPhone(phone: String): Single<CheckExistPhoneResponse> {
        return authApi.checkExistPhone(phone).compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun sendTmpPassword(phone: String): Single<RemainingAttemptsResponse> {
        return authApi.sendTmpPassword(phone).compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun changePasswordBySmsCode(
        phone: String,
        password: String,
        tmpPassword: String
    ): Completable {
        val requestBody = ChangePasswordBySmsCodeQuery(password, tmpPassword)
        return authApi.changePasswordBySmsCode(
            phone, requestBody
        ).compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun passwordCheck(phone: String, tmpPassword: String): Completable {
        return authApi.passwordCheck(phone, PasswordCheckQuery(tmpPassword))
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun statistics(): Single<StatisticsResponse> {
        return authApi.statistics().compose(rxSchedulerFactory.applySingleSchedulers())
    }

}