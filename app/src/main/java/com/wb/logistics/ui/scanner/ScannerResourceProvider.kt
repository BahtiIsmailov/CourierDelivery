package com.wb.logistics.ui.scanner

import android.content.Context
import com.wb.logistics.R

class ScannerResourceProvider(private val context: Context) {

    fun getBarCodeBox(code: String) : String = context.getString(R.string.reception_code_box, code)

}