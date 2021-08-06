package ru.wb.perevozka.ui.unloadingforcedtermination

import android.content.Context
import ru.wb.perevozka.R

class ForcedTerminationResourceProvider(private val context: Context) {

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

    fun getDataLogFormat(event: String, message: String): String =
        context.getString(R.string.unloading_box_log_data, event, message)

    fun getBoxDialogTitle(): String =
        context.getString(R.string.unloading_boxes_dialog_complete_title)

    fun getErrorCompleteUnloading(): String =
        context.getString(R.string.unloading_boxes_dialog_complete_error)

    fun getBoxPositiveButton() =
        context.getString(R.string.unloading_boxes_dialog_complete_ok)

    fun getIndexUnnamedBarcode(index: Int, barcode: String): String {
        return context.getString(R.string.unnamed_barcode_format_number,
            index,
            barcode.take(4),
            barcode.takeLast(4))
    }

}