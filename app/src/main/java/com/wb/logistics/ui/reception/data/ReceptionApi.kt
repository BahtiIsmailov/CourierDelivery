package com.wb.logistics.ui.reception.data

import com.wb.logistics.ui.reception.data.delivery.Reception
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface ReceptionApi {

    @GET("template")
    fun getTemplateAsync(
        @Query("") apiKey1: String,
        @Query("") apiKey2: String
    ): Deferred<Reception>

}