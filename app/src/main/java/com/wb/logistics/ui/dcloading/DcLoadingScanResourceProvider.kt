package com.wb.logistics.ui.dcloading

import android.content.Context
import com.wb.logistics.R

class DcLoadingScanResourceProvider(private val context: Context) {

    fun getShortAddedBox(code: String) : String = context.getString(R.string.dc_loading_code_short_added_box, code)
    fun getShortHasBeenAddedBox(code: String) : String = context.getString(R.string.dc_loading_code_short_has_been_added_box, code)
    fun getBoxNotBelongDcToolbarTitle() : String = context.getString(R.string.dc_loading_box_not_belong_dc_toolbar_title)
    fun getBoxNotBelongDcTitle() : String = context.getString(R.string.dc_loading_box_not_belong_dc_title)
    fun getBoxNotBelongFlightTitle() : String = context.getString(R.string.dc_loading_box_not_belong_flight_title)
    fun getBoxNotBelongFlightToolbarTitle() : String = context.getString(R.string.dc_loading_box_not_belong_flight_toolbar_title)
    fun getBoxNotBelongAddress() : String = context.getString(R.string.dc_loading_box_not_belong_address_title)

}