package ru.wb.go.network.api.auth

import io.reactivex.Single
import retrofit2.http.*
import ru.wb.go.network.api.auth.query.AuthBySmsOrPasswordQuery
import ru.wb.go.network.api.auth.query.RefreshTokenQuery
import ru.wb.go.network.api.auth.response.AuthResponse
import ru.wb.go.network.api.auth.response.CheckCouriersPhoneResponse
import ru.wb.go.network.api.auth.response.StatisticsResponse

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

    @GET("{version}/statistics")
    fun statistics(
        @Path(value = "version", encoded = true) version: String,
    ): Single<StatisticsResponse>

}