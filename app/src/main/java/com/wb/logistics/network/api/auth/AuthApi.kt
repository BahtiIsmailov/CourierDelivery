package com.wb.logistics.network.api.auth

import com.wb.logistics.network.api.auth.query.AuthByPhoneOrPasswordQuery
import com.wb.logistics.network.api.auth.query.ChangePasswordBySmsCodeQuery
import com.wb.logistics.network.api.auth.query.PasswordCheckQuery
import com.wb.logistics.network.api.auth.response.AuthResponse
import com.wb.logistics.network.api.auth.response.CheckExistPhoneResponse
import com.wb.logistics.network.api.auth.response.RemainingAttemptsResponse
import com.wb.logistics.network.api.auth.response.StatisticsResponse
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface AuthApi {

    @POST("{version}/auth")
    fun authByPhoneOrPassword(
        @Path(value = "version", encoded = true) version: String,
        @Body authByPhoneOrPasswordQuery: AuthByPhoneOrPasswordQuery,
    ): Single<AuthResponse>

    @GET("{version}/auth/{phone}")
    fun checkExistPhone(
        @Path(value = "version", encoded = true) version: String,
        @Path("phone") phone: String,
    ): Single<CheckExistPhoneResponse>

    @GET("{version}/auth/{phone}/password")
    fun sendTmpPassword(
        @Path(value = "version", encoded = true) version: String,
        @Path("phone") phone: String,
    ): Single<RemainingAttemptsResponse>

    @PUT("{version}/auth/{phone}/password")
    fun changePasswordBySmsCode(
        @Path(value = "version", encoded = true) version: String,
        @Path("phone") phone: String,
        @Body changePasswordBySmsCodeQuery: ChangePasswordBySmsCodeQuery,
    ): Completable

    @POST("{version}/auth/{phone}/password/check")
    fun passwordCheck(
        @Path(value = "version", encoded = true) version: String,
        @Path("phone") phone: String,
        @Body passwordCheckQuery: PasswordCheckQuery,
    ): Completable

    @GET("{version}/statistics")
    fun statistics(
        @Path(value = "version", encoded = true) version: String,
    ): Single<StatisticsResponse>

}