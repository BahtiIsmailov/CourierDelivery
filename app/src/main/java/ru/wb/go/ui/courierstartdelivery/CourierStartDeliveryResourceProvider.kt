package ru.wb.go.ui.courierstartdelivery

import android.content.Context
import ru.wb.go.R

class CourierStartDeliveryResourceProvider(private val context: Context) {

    fun getAmountInfo(amount: Int): String =
        context.getString(R.string.courier_start_delivery_amount, amount)

    fun getDeliverLoadCountInfo(delivery: Int): String =
        context.getString(R.string.courier_start_delivery_count, delivery)
}