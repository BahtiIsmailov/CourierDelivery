package ru.wb.go.ui.flights.domain

import ru.wb.go.db.FlightData
import ru.wb.go.db.Optional
import io.reactivex.Flowable

interface FlightsInteractor {
    fun observeFlightData(): Flowable<Optional<FlightData>>
    fun observeFlightBoxScanned(): Flowable<Int>
}