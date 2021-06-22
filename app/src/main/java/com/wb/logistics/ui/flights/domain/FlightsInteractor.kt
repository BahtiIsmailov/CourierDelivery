package com.wb.logistics.ui.flights.domain

import com.wb.logistics.db.FlightData
import com.wb.logistics.db.Optional
import io.reactivex.Flowable

interface FlightsInteractor {
    fun observeFlightData(): Flowable<Optional<FlightData>>
    fun observeFlightBoxScanned(): Flowable<Int>
}