package ru.wb.go.ui.dcunloadingcongratulation

import android.content.Context
import ru.wb.go.R

class DcUnloadingCongratulationResourceProvider(private val context: Context) {

    fun getInfo(delivery: Int, from: Int): String =
        context.getString(R.string.dc_congratulation_count, delivery, from)

    fun getScanDialogTitle() = context.getString(R.string.dc_congratulation_dialog_title_error)
    fun getScanDialogMessage() = context.getString(R.string.dc_congratulation_dialog_message_error)
    fun getScanDialogButton() =
        context.getString(R.string.dc_congratulation_dialog_positive_button_error)

}