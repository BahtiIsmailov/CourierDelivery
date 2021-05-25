package com.wb.logistics.ui.dcforcedtermination

import android.content.Context
import com.wb.logistics.R

class DcForcedTerminationDetailsResourceProvider(private val context: Context) {

    fun getNotDelivery(date: String, time: String) =
        context.getString(R.string.dc_forced_termination_details_delivery_data, date, time)

    fun getNotReturned(date: String, time: String, address: String) =
        context.getString(R.string.dc_forced_termination_details_return_data, date, time, address)

}