package com.wb.logistics.network.api

import io.reactivex.Completable
import retrofit2.http.GET
import retrofit2.http.Query

interface AuthApi {

    @GET("template")
    fun getTemplateAsync(
        @Query("") apiKey1: String,
        @Query("") apiKey2: String
    ): Completable

}