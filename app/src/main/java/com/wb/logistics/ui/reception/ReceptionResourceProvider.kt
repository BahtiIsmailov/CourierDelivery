package com.wb.logistics.ui.reception

import android.content.Context
import com.wb.logistics.R

class ReceptionResourceProvider(private val context: Context) {

    fun getCodeBox(code: String) : String = context.getString(R.string.reception_code_box, code)
    fun getAddedBox(code: String) : String = context.getString(R.string.reception_code_added_box, code)
    fun getBoxNotBelongDcTitle() : String = context.getString(R.string.reception_box_not_belong_dc_title)
    fun getBoxNotBelongFlightTitle() : String = context.getString(R.string.reception_box_not_belong_flight_title)
    fun getBoxNotBelongAddressTitle() : String = context.getString(R.string.reception_box_not_belong_address_title)

}