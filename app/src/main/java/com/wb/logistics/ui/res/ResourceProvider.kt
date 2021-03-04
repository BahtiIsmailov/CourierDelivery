package com.wb.logistics.ui.res

import android.content.Context
import com.wb.logistics.R

class ResourceProvider(private val context: Context) {

    fun getVersionApp(version: String) = context.getString(R.string.app_version, version)

    fun getEmptyFlight() : String = context.getString(R.string.delivery_empty_flight)

}