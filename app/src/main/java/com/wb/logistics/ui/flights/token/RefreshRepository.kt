package com.wb.logistics.ui.flights.token

import com.wb.logistics.network.api.auth.query.RefreshTokenQuery
import com.wb.logistics.network.api.auth.response.RefreshResponse
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PUT

interface RefreshRepository {

    @PUT("/api/v1/auth")
    fun refreshToken(
        @Body refreshTokenQuery: RefreshTokenQuery
    ): Single<RefreshResponse>

    @PUT("/api/v1/auth")
    fun refreshAccessTokens(
        @Body refreshTokenQuery: RefreshTokenQuery
    ): Call<RefreshResponse>

}
