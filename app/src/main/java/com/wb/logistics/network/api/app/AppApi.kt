package com.wb.logistics.network.api.app

import com.wb.logistics.network.api.app.response.FlightStatusesResponse
import com.wb.logistics.network.api.app.response.FlightsResponse
import io.reactivex.Single
import retrofit2.http.GET

interface AppApi {

    //Flights
    @GET("/api/v1/flight-statuses")
    fun flightStatuses(): Single<FlightStatusesResponse>

    @GET("/api/v1/flights")
    fun flights(): Single<FlightsResponse>

}