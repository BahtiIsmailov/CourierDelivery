package com.wb.logistics.ui.reception

import android.content.Context
import com.wb.logistics.R

class ReceptionResourceProvider(private val context: Context) {

    fun getCodeBox(code: String) : String = context.getString(R.string.reception_code_box, code)

}