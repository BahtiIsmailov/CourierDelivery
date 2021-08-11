package ru.wb.perevozka.network.api.auth

import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.perevozka.network.api.auth.entity.UserInfoEntity
import ru.wb.perevozka.network.api.auth.response.CheckExistPhoneResponse
import ru.wb.perevozka.network.api.auth.response.RemainingAttemptsResponse
import ru.wb.perevozka.network.api.auth.response.StatisticsResponse

interface AuthRemoteRepository {

    fun auth(password: String, phone: String, useSMS: Boolean): Completable

    fun couriersExistAndSavePhone(phone: String): Completable

    fun couriersForm(phone: String): Completable

    fun refreshToken(): Completable

    @Deprecated("")
    fun checkExistAndSavePhone(phone: String): Single<CheckExistPhoneResponse>

    @Deprecated("")
    fun sendTmpPassword(phone: String): Single<RemainingAttemptsResponse>

    @Deprecated("")
    fun passwordCheck(phone: String, tmpPassword: String): Completable

    @Deprecated("")
    fun changePasswordBySmsCode(phone: String, password: String, tmpPassword: String): Completable

    fun statistics(): Single<StatisticsResponse>

    fun userInfo(): Single<UserInfoEntity>

    fun clearToken()

    fun userPhone(): String

}