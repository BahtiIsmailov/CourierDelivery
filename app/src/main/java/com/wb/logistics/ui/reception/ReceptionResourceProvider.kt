package com.wb.logistics.ui.reception

import android.content.Context
import com.wb.logistics.R

class ReceptionResourceProvider(private val context: Context) {

    fun getBarCodeDivider(code: String) : String = context.getString(R.string.reception_code_box_divider, code)
    fun getBarCodeBox(code: String) : String = context.getString(R.string.reception_code_box, code)
    fun getAddedBox(code: String) : String = context.getString(R.string.reception_code_added_box, code)
    fun getShortAddedBox(code: String) : String = context.getString(R.string.reception_code_short_added_box, code)
    fun getShortHasBeenAddedBox(code: String) : String = context.getString(R.string.reception_code_short_has_been_added_box, code)
    fun getBoxNotBelongDcToolbarTitle() : String = context.getString(R.string.reception_box_not_belong_dc_toolbar_title)
    fun getBoxNotBelongDcTitle() : String = context.getString(R.string.reception_box_not_belong_dc_title)
    fun getBoxNotBelongFlightTitle() : String = context.getString(R.string.reception_box_not_belong_flight_title)
    fun getBoxNotBelongFlightToolbarTitle() : String = context.getString(R.string.reception_box_not_belong_flight_toolbar_title)
    fun getBoxNotBelongAddress() : String = context.getString(R.string.reception_box_not_belong_address_title)

}