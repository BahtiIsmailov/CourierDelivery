package com.wb.logistics.ui.flights.token

import com.wb.logistics.app.APIConstants.BASE_API_URL
import com.wb.logistics.network.api.auth.query.RefreshTokenQuery
import com.wb.logistics.network.api.auth.response.RefreshResponse
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RefreshRepositoryImpl : RefreshRepository {

    private var repository: RefreshRepository

    init {
        val headerInterceptor = HttpLoggingInterceptor()
        headerInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS)
        val bodyInterceptor = HttpLoggingInterceptor()
        bodyInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(bodyInterceptor)
            .build()
        val retrofit = Retrofit.Builder().baseUrl(BASE_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        repository = retrofit.create(RefreshRepository::class.java)
    }

    override fun refreshToken(refreshTokenQuery: RefreshTokenQuery): Single<RefreshResponse> {
        return repository.refreshToken(refreshTokenQuery)
    }

    override fun refreshAccessTokens(refreshTokenQuery: RefreshTokenQuery): Call<RefreshResponse> {
        return repository.refreshAccessTokens(refreshTokenQuery)
    }

//    override fun getCodeByPhone(
//        credentials: String,
//        body: CodeRequest
//    ): Call<ResponseBody> = repository.getCodeByPhone(credentials, body)
//
//    override fun getAccessTokens(credentials: String,
//                                 body: TokenRequest
//    ): Call<Token> =
//        repository.getAccessTokens(credentials, body)
//
//    override fun refreshAccessTokens(accessToken: String,
//                                     body: RefreshRequest
//    ): Call<Token> {
//        return repository.refreshAccessTokens("Bearer $accessToken", body)
//    }
//
//    override suspend fun login(body: Credentials): Response<Token> {
//        return repository.login(body)
//    }
}