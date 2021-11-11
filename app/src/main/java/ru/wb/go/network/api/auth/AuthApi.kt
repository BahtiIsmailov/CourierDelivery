package ru.wb.go.network.api.auth

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*
import ru.wb.go.network.api.auth.query.AuthBySmsOrPasswordQuery
import ru.wb.go.network.api.auth.query.ChangePasswordBySmsCodeQuery
import ru.wb.go.network.api.auth.query.PasswordCheckQuery
import ru.wb.go.network.api.auth.query.RefreshTokenQuery
import ru.wb.go.network.api.auth.response.*

interface AuthApi {

    @POST("{version}/auth")
    fun auth(
        @Path(value = "version", encoded = true) version: String,
        @Body authByPhoneOrPasswordQuery: AuthBySmsOrPasswordQuery,
    ): Single<AuthResponse>

    @GET("{version}/couriers-auth/{phone}/password")
    fun couriersAuth(
        @Path(value = "version", encoded = true) version: String,
        @Path("phone") phone: String,
    ): Single<CheckCouriersPhoneResponse>

    @PUT("{version}/auth")
    fun refreshToken(
        @Path(value = "version", encoded = true) version: String,
        @Body refreshTokenQuery: RefreshTokenQuery
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