package com.wb.logistics.network.rest

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RefreshTokenRetrofitFactory(
    private val apiServer: String,
    private val okHttpClient: OkHttpClient,
    private val gsonConverterFactory: GsonConverterFactory,
) {
    fun <T> getApiInterface(apiClass: Class<T>): T {
        return Retrofit.Builder()
            .baseUrl(apiServer)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(apiClass)
    }
}