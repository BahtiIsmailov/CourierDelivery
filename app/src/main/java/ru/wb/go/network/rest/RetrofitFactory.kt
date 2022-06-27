package ru.wb.go.network.rest

import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.wb.go.network.NullOnEmptyConverterFactory

class RetrofitFactory(
    private val baseUrlServer: String,
    private val okHttpClient: OkHttpClient,
    private val nullOnEmptyConverterFactory: NullOnEmptyConverterFactory,
    private val gsonConverterFactory: GsonConverterFactory,
) {
    fun <T> getApiInterface(apiClass: Class<T>): T {
        return Retrofit.Builder()
            .baseUrl(baseUrlServer)
            .client(okHttpClient)
            .addConverterFactory(nullOnEmptyConverterFactory)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(apiClass)
    }

    fun <T> getApiDynamicInterface(apiClass: Class<T>): T {
        return Retrofit.Builder()
            .baseUrl(baseUrlServer)
            .client(okHttpClient)
            .addConverterFactory(nullOnEmptyConverterFactory)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(apiClass)
    }
}