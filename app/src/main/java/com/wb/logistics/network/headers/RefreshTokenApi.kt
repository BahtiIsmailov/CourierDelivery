package com.wb.logistics.network.headers

import com.wb.logistics.network.api.auth.query.RefreshTokenQuery
import com.wb.logistics.network.api.auth.response.RefreshResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PUT

interface RefreshTokenApi {

    @PUT("/api/v1/auth")
    fun refreshAccessTokens(
        @Header("Authorization") credentials: String,
        @Body refreshTokenQuery: RefreshTokenQuery
    ): Call<RefreshResponse>

}
