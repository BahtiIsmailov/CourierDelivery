package com.wb.logistics.ui.scanner

import android.content.Context
import com.wb.logistics.R

class ScannerResourceProvider(private val context: Context) {

    fun getBarCodeBox(code: String) =
        context.getString(R.string.dc_loading_code_box, code)

}