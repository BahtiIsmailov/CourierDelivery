package com.wb.logistics.network.api.app

import com.wb.logistics.network.api.app.response.FlightStatusesResponse
import com.wb.logistics.network.api.app.response.FlightsResponse
import io.reactivex.Single

interface AppRepository {

    fun flightStatuses(): Single<FlightStatusesResponse>

    fun flights(): Single<FlightsResponse>

}