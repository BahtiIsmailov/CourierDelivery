package com.wb.logistics.ui.flights.data

import com.wb.logistics.ui.flights.data.delivery.Delivery
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface FlightsApi {

    @GET("template")
    fun getTemplateAsync(
        @Query("") apiKey1: String,
        @Query("") apiKey2: String
    ): Deferred<Delivery>

}