package com.wb.logistics.ui.unloadingforcedtermination

import android.content.Context
import com.wb.logistics.R

class ForcedTerminationResourceProvider(private val context: Context) {

    fun getLabel() =
        context.getString(R.string.force_termination_label)

    fun getNotDeliveryTitle(count: Int) =
        context.getString(R.string.force_termination_not_delivery_count, count)

    fun getBoxNotFound() =
        context.getString(R.string.force_termination_box_not_found)

    fun getNotPickupPoint() =
        context.getString(R.string.force_termination_pickup_point)

    fun getEmpty() =
        context.getString(R.string.force_termination_empty)

    fun getNotDeliveryDate(date: String, time: String) =
        context.getString(R.string.force_termination_not_delivery_data, date, time)

    fun getDataLogFormat(event: String, message: String): String =
        context.getString(R.string.unloading_box_log_data, event, message)

}