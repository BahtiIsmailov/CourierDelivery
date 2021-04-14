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

    @POST("/api/v1/auth")
    fun authByPhoneOrPassword(
        @Body authByPhoneOrPasswordQuery: AuthByPhoneOrPasswordQuery
    ): Single<AuthResponse>

    @GET("/api/v1/auth/{phone}")
    fun checkExistPhone(
        @Path("phone") phone: String
    ): Single<CheckExistPhoneResponse>

    @GET("/api/v1/auth/{phone}/password")
    fun sendTmpPassword(
        @Path("phone") phone: String
    ): Single<RemainingAttemptsResponse>

    @PUT("/api/v1/auth/{phone}/password")
    fun changePasswordBySmsCode(
        @Path("phone") phone: String,
        @Body changePasswordBySmsCodeQuery: ChangePasswordBySmsCodeQuery
    ): Completable

    @POST("/api/v1/auth/{phone}/password/check")
    fun passwordCheck(
        @Path("phone") phone: String,
        @Body passwordCheckQuery: PasswordCheckQuery
    ): Completable

    @GET("/api/v1/statistics")
    fun statistics(): Single<StatisticsResponse>

}