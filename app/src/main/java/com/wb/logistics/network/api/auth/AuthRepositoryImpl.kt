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
import com.wb.logistics.utils.LogUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val tokenManager: TokenManager
) : AuthRepository {

    override fun authByPhoneOrPassword(
        password: String, phone: String, useSMS: Boolean
    ): Completable {
        val requestBody = AuthByPhoneOrPasswordQuery(phone, password, useSMS)
        val auth = authApi.authByPhoneOrPassword(requestBody)
            .map { TokenEntity(it.accessToken, it.expiresIn, it.refreshToken) }
            .doOnSuccess { saveToken(it) }
        return Completable.fromSingle(auth).compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun refreshToken(): Completable {
        return Completable.complete()
        // TODO: 22.03.2021 в тестирование
//        val refreshToken = authApi.refreshToken(RefreshTokenQuery(tokenManager.bearerRefreshToken()))
//            .map { TokenEntity(it.accessToken, it.expiresIn, it.refreshToken) }
//            .doOnSuccess { saveToken(it) }
//            .retryWhen { retryOnError(it) }
//
//        return Completable.fromSingle(refreshToken)
//            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun retryOnError(throwable: Flowable<Throwable>): Flowable<Long?>? {
        return throwable
            .zipWith(Flowable.range(START_COUNT_REPEAT, COUNT_REPEAT), retryCount())
            .flatMap { retryTime() }
    }

    private fun retryCount() = { throwable: Throwable, retryCount: Int ->
        LogUtils { logDebugApp("PUSH_TOKEN_TAG error getRetryByError: $throwable error count: $retryCount") }
        retryCount
    }

    private fun retryTime() = Flowable.timer(DELAY_REPEAT_TIME, TimeUnit.SECONDS)
        .doOnNext { aLong: Long ->
            LogUtils { logDebugApp("PUSH_TOKEN_TAG error doOnNext timer: $aLong") }
        }
        .doOnComplete {
            LogUtils { logDebugApp("PUSH_TOKEN_TAG error doOnComplete timer") }
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
        tmpPassword: String
    ): Completable {
        val requestBody = ChangePasswordBySmsCodeQuery(password, tmpPassword)
        return authApi.changePasswordBySmsCode(
            phone, requestBody
        ).compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun passwordCheck(
        phone: String,
        tmpPassword: String
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

    companion object {
        const val START_COUNT_REPEAT = 1
        const val COUNT_REPEAT = 3
        const val DELAY_REPEAT_TIME = 5L
    }

}