package com.wb.logistics.network.api.auth

import com.wb.logistics.network.api.auth.response.CheckExistPhoneResponse
import com.wb.logistics.network.api.auth.response.RemainingAttemptsResponse
import com.wb.logistics.network.api.auth.response.StatisticsResponse
import io.reactivex.Completable
import io.reactivex.Single

interface AuthRepository {

    fun authByPhoneOrPassword(
        password: String,
        phone: String,
        useSMS: Boolean
    ): Completable

    fun checkExistPhone(phone: String): Single<CheckExistPhoneResponse>

    fun sendTmpPassword(phone: String): Single<RemainingAttemptsResponse>

    fun passwordCheck(phone: String, tmpPassword: String): Completable

    fun changePasswordBySmsCode(phone: String, password: String, tmpPassword: String): Completable

    fun statistics(): Single<StatisticsResponse>

    fun userInfo() : Single<Pair<String, String>>

}