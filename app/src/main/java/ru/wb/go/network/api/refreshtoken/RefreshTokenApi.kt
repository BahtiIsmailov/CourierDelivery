package ru.wb.go.network.api.refreshtoken

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PUT
import ru.wb.go.network.api.auth.query.RefreshTokenQuery
import ru.wb.go.network.api.auth.response.RefreshResponse

interface RefreshTokenApi {

    @PUT("/api/v1/auth")
     suspend fun refreshAccessTokens(
        @Header("Authorization") credentials: String,
        @Body refreshTokenQuery: RefreshTokenQuery
    ): Response<RefreshResponse>

}
