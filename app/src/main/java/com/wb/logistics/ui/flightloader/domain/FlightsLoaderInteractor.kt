package com.wb.logistics.ui.flightloader.domain

import com.wb.logistics.db.FlightData
import com.wb.logistics.db.SuccessOrEmptyData
import io.reactivex.Single

interface FlightsLoaderInteractor {
    fun updateFlight() : Single<SuccessOrEmptyData<FlightData>>
}