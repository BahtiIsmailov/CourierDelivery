package com.wb.logistics.ui.res

import android.content.Context
import com.wb.logistics.R

class AppResourceProvider(private val context: Context) {

    fun getVersionApp(version: String) = context.getString(R.string.app_version, version)

    fun getEmptyFlight() : String = context.getString(R.string.flights_empty_flight)

    fun getErrorFlight() : String = context.getString(R.string.flights_error_flight)

    fun getZeroFlight() : String = context.getString(R.string.flights_zero)

    fun getOneFlight() : String = context.getString(R.string.flights_one)

}