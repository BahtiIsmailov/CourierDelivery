package com.wb.logistics.ui.flights.domain

import com.wb.logistics.db.FlightData
import com.wb.logistics.db.SuccessOrEmptyData
import io.reactivex.Completable
import io.reactivex.Flowable

interface FlightsInteractor {
    fun updateFlight()
    fun flight(): Completable
    fun observeFlight(): Flowable<SuccessOrEmptyData<FlightData>>
    fun observeFlightBoxScanned(): Flowable<Int>
    fun removeBoxesToFlight()
}