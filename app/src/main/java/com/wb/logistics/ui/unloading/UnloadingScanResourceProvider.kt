package com.wb.logistics.ui.unloading

import android.content.Context
import com.wb.logistics.R

class UnloadingScanResourceProvider(private val context: Context) {

    fun getNumericBarcode(number: Int, barcode: String): String =
        context.getString(R.string.unloading_boxes_numeric_barcode, number, barcode)

    fun getBoxNotBelongPointToolbarTitle(): String =
        context.getString(R.string.unloading_box_not_belong_point_toolbar_label)

    fun getBoxNotBelongPointTitle(): String =
        context.getString(R.string.unloading_box_not_belong_dc_title)

}