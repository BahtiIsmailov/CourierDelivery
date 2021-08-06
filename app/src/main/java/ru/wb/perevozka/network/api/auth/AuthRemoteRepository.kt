package ru.wb.perevozka.network.api.auth

import ru.wb.perevozka.network.api.auth.entity.UserInfoEntity
import ru.wb.perevozka.network.api.auth.response.CheckExistPhoneResponse
import ru.wb.perevozka.network.api.auth.response.RemainingAttemptsResponse
import ru.wb.perevozka.network.api.auth.response.StatisticsResponse
import io.reactivex.Completable
import io.reactivex.Single

interface AuthRemoteRepository {

    fun authByPhoneOrPassword(
        password: String,
        phone: String,
        useSMS: Boolean,
    ): Completable

    fun checkExistAndSavePhone(phone: String): Single<CheckExistPhoneResponse>

    fun sendTmpPassword(phone: String): Single<RemainingAttemptsResponse>

    fun passwordCheck(phone: String, tmpPassword: String): Completable

    fun changePasswordBySmsCode(phone: String, password: String, tmpPassword: String): Completable

    fun statistics(): Single<StatisticsResponse>

    fun userInfo(): Single<UserInfoEntity>

    fun clearToken()

    fun userPhone(): String

}