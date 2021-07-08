package com.wb.logistics.ui.dcunloadingforcedtermination

import android.content.Context
import com.wb.logistics.R

class DcForcedTerminationResourceProvider(private val context: Context) {

    fun getLabel() =
        context.getString(R.string.force_termination_label)

    fun getNotDeliveryTitle(count: Int) =
        context.getString(R.string.force_termination_not_delivery_count, count)

    fun getBoxNotFound() =
        context.getString(R.string.force_termination_box_not_found)

    fun getEmpty() =
        context.getString(R.string.force_termination_empty)

    fun getNotDeliveryDate(date: String, time: String) =
        context.getString(R.string.force_termination_not_delivery_data, date, time)

    fun getDataLogFormat(message: String): String =
        context.getString(R.string.unloading_box_log_data, getLabel(), message)

    fun getForcedDialogTitle() = context.getString(R.string.dc_unloading_forced_termination_dialog_title_error)
    fun getForcedDialogMessage() = context.getString(R.string.dc_unloading_forced_termination_dialog_message_error)
    fun getForcedDialogButton() = context.getString(R.string.dc_unloading_forced_termination_dialog_positive_button_error)

}