package com.wb.logistics.ui.delivery.data

import com.wb.logistics.ui.delivery.data.delivery.Delivery
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface DeliveryApi {

    @GET("template")
    fun getTemplateAsync(
        @Query("") apiKey1: String,
        @Query("") apiKey2: String
    ): Deferred<Delivery>

}