package com.wb.logistics.ui.flightsempty

import android.content.Context
import com.wb.logistics.R

class FlightsEmptyResourceProvider(private val context: Context) {

    fun getZeroFlight() : String = context.getString(R.string.flights_zero)

    fun getOneFlight() : String = context.getString(R.string.flights_one)

    fun getRoutesTitle(title: String) : String = context.getString(R.string.flights_routes_title_pref, title)

}