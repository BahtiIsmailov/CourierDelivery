package com.wb.logistics.ui.unloadingcongratulation

import android.content.Context
import com.wb.logistics.R
import kotlin.math.abs

class CongratulationResourceProvider(private val context: Context) {

    fun getInfo(delivery: Int, from: Int, points: Int): String =
        context.getString(R.string.congratulation_info, delivery, getFrom(from), getPoint(points))

    private fun getFrom(count: Int): String = context.resources
        .getQuantityString(R.plurals.delivery_box_count, abs(count), count)

    private fun getPoint(count: Int): String = context.resources
        .getQuantityString(R.plurals.delivery_point_count, abs(count), count)

}