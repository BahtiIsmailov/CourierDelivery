package ru.wb.go.ui.couriercompletedelivery

import android.content.Context
import ru.wb.go.R

class CourierCompleteDeliveryResourceProvider(private val context: Context) {

    fun getAmountInfo(amount: Int): String =
        context.getString(R.string.courier_complete_delivery_amount, amount)

    fun getDeliveredInfo(delivery: Int, from: Int): String =
        context.getString(R.string.courier_complete_delivery_count, delivery, from)
}