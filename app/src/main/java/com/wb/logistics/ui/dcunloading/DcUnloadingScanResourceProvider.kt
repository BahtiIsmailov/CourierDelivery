package com.wb.logistics.ui.dcunloading

import android.content.Context
import com.wb.logistics.R

class DcUnloadingScanResourceProvider(private val context: Context) {

    fun getBoxNotFoundTitle(): String =
        context.getString(R.string.dc_unloading_box_not_found_title)

    fun getBoxAlreadyUnloaded(barcode: String): String =
        context.getString(R.string.dc_unloading_box_already_unloaded, barcode)

    fun getBoxUnloaded(barcode: String): String =
        context.getString(R.string.dc_unloading_box_unloaded, barcode)


}