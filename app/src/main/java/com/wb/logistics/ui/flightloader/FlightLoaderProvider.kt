package com.wb.logistics.ui.flightloader

import android.content.Context
import com.wb.logistics.R

class FlightLoaderProvider(private val context: Context) {

    fun getEmptyFlight() : String = context.getString(R.string.flights_zero)

    fun getOneFlight() : String = context.getString(R.string.flights_one)

}