package com.wb.logistics.ui.unloadingcongratulation

import android.content.Context
import com.wb.logistics.R

class CongratulationResourceProvider(private val context: Context) {

    fun getInfo(delivery: Int, from: Int): String =
        context.getString(R.string.congratulation_info_count, delivery, from)

}