package com.wb.logistics.ui.flights

import android.content.Context
import com.wb.logistics.R

class FlightsResourceProvider(private val context: Context) {

    fun getEmptyFlight() : String = context.getString(R.string.flights_empty_flight)

    fun getErrorFlight() : String = context.getString(R.string.flights_error_flight)

    fun getZeroFlight() : String = context.getString(R.string.flights_zero)

    fun getOneFlight() : String = context.getString(R.string.flights_one)

    fun getFlightNumber(flight: Int) : String = context.getString(R.string.flights_flight_number, flight)

    fun getParkingNumber(parkingNumber: Int) : String = context.getString(R.string.flights_parking_number, parkingNumber)

    fun getRoutesTitle(title: String) : String = context.getString(R.string.flights_routes_title_pref, title)

    fun getRoutesEmpty() : String = context.getString(R.string.flights_routes_empty)

}