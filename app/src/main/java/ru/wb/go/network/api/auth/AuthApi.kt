package ru.wb.go.network.api.auth

import retrofit2.http.*
import ru.wb.go.network.api.auth.query.AuthBySmsOrPasswordQuery
import ru.wb.go.network.api.auth.query.RefreshTokenQuery
import ru.wb.go.network.api.auth.response.AuthResponse
import ru.wb.go.network.api.auth.response.CheckCouriersPhoneResponse
import ru.wb.go.network.api.auth.response.StatisticsResponse

interface AuthApi {

    @POST(" ")
    suspend fun auth(
        @Path(value = "version", encoded = true) version: String,
        @Body authByPhoneOrPasswordQuery: AuthBySmsOrPasswordQuery,
    ): AuthResponse

    @GET(" ")
    suspend fun couriersAuth(
        @Path(value = "version", encoded = true) version: String,
        @Path("phone") phone: String,
    ):  CheckCouriersPhoneResponse

    @POST(" ")
    suspend fun refreshToken(
        @Path(value = "version", encoded = true) version: String,
        @Body refreshTokenQuery: RefreshTokenQuery
    ):  AuthResponse

    @GET("")
    suspend fun statistics(
        @Path(value = "version", encoded = true) version: String,
    ):  StatisticsResponse

}