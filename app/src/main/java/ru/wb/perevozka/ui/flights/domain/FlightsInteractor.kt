package ru.wb.perevozka.ui.flights.domain

import ru.wb.perevozka.db.FlightData
import ru.wb.perevozka.db.Optional
import io.reactivex.Flowable

interface FlightsInteractor {
    fun observeFlightData(): Flowable<Optional<FlightData>>
    fun observeFlightBoxScanned(): Flowable<Int>
}