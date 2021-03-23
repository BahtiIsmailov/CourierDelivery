package com.wb.logistics.network.api.app

import com.wb.logistics.network.api.app.response.FlightResponse
import com.wb.logistics.network.api.app.response.FlightStatuses
import io.reactivex.Single
import retrofit2.http.GET

interface AppApi {

    //Flights
    @GET("/api/v1/flight-statuses")
    fun flightStatuses(): Single<FlightStatuses>

    @GET("/api/v1/flight")
    fun flight(): Single<FlightResponse>

}