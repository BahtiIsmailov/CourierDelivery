package com.wb.logistics.ui.dcunloadingcongratulation

import android.content.Context
import com.wb.logistics.R

class DcUnloadingCongratulationResourceProvider(private val context: Context) {

    fun getInfo(delivery: Int, from: Int): String =
        context.getString(R.string.dc_congratulation_count, delivery, from)

}