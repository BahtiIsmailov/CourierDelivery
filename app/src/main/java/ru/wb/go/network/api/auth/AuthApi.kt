package ru.wb.go.network.api.auth

import retrofit2.http.*
import ru.wb.go.network.api.auth.query.AuthBySmsOrPasswordQuery
import ru.wb.go.network.api.auth.query.RefreshTokenQuery
import ru.wb.go.network.api.auth.response.AuthResponse
import ru.wb.go.network.api.auth.response.CheckCouriersPhoneResponse
import ru.wb.go.network.api.auth.response.StatisticsResponse

interface AuthApi {

    @POST("{version}/auth")
    suspend fun auth(
        @Path(value = "version", encoded = true) version: String,
        @Body authByPhoneOrPasswordQuery: AuthBySmsOrPasswordQuery,
    ): AuthResponse

    @GET("{version}/couriers-auth/{phone}/password")
    suspend fun couriersAuth(
        @Path(value = "version", encoded = true) version: String,
        @Path("phone") phone: String,
    ):  CheckCouriersPhoneResponse

    @PUT("{version}/auth")
    suspend fun refreshToken(
        @Path(value = "version", encoded = true) version: String,
        @Body refreshTokenQuery: RefreshTokenQuery
    ):  AuthResponse

    @GET("{version}/statistics")
    suspend fun statistics(
        @Path(value = "version", encoded = true) version: String,
    ):  StatisticsResponse

}