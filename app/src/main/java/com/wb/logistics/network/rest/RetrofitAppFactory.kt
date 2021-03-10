package com.wb.logistics.network.rest

import com.wb.logistics.network.NullOnEmptyConverterFactory
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitAppFactory(
    private val apiServer: String,
    private val okHttpClient: OkHttpClient,
    private val callAdapterFactory: CallAdapter.Factory,
    nullOnEmptyConverterFactory: NullOnEmptyConverterFactory,
    gsonConverterFactory: GsonConverterFactory
) {
    private val nullOnEmptyConverterFactory: Converter.Factory
    private val gsonConverterFactory: GsonConverterFactory
    fun <T> getApiInterface(apiClass: Class<T>): T {
        return Retrofit.Builder()
            .addCallAdapterFactory(callAdapterFactory)
            .baseUrl(apiServer)
            .client(okHttpClient)
            .addConverterFactory(nullOnEmptyConverterFactory)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(apiClass)
    }

    init {
        this.nullOnEmptyConverterFactory = nullOnEmptyConverterFactory
        this.gsonConverterFactory = gsonConverterFactory
    }
}