package com.wb.logistics.network.api.app

import com.wb.logistics.network.api.app.response.FlightResponse
import com.wb.logistics.network.api.app.response.FlightStatuses
import io.reactivex.Single

interface AppRepository {

    fun flightStatuses(): Single<FlightStatuses>

    fun flight(): Single<FlightResponse>

}