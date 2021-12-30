package ru.wb.go.ui.courierbilllingcomplete

import android.content.Context
import ru.wb.go.R

class CourierBillingCompleteResourceProvider(private val context: Context) {

    fun getTitle(amount: Int): String =
        context.getString(R.string.billing_complete_title, amount)

}