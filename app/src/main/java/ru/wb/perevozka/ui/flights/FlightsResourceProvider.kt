package ru.wb.perevozka.ui.flights

import android.content.Context
import ru.wb.perevozka.R

class FlightsResourceProvider(private val context: Context) {

    fun getEmptyFlight() : String = context.getString(R.string.flights_empty_flight)

    fun getErrorFlight() : String = context.getString(R.string.flights_error_flight)

    fun getFlightNumber(flight: Int) : String = context.getString(R.string.flights_flight_number, flight)

    fun getParkingNumber(parkingNumber: Int) : String = context.getString(R.string.flights_parking_number, parkingNumber)

    fun getRoutesTitle(title: String) : String = context.getString(R.string.flights_routes_title_pref, title)

    fun getRoutesEmpty() : String = context.getString(R.string.flights_routes_empty)

}