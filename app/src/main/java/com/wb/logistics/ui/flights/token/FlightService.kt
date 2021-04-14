package com.wb.logistics.ui.flights.token

import com.wb.logistics.network.api.app.remote.flight.FlightRemote
import retrofit2.Call
import retrofit2.http.GET

interface FlightService {

    @GET("/api/v1/flight")
    fun flight(): Call<FlightRemote>

//    @GET("trips/boxinoffice")
//    suspend fun getOffice(@Query("tripID") tripID: Int,
//                          @Query("officeID") officeID: Int): To

}