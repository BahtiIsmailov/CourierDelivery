package ru.wb.perevozka.ui.courierbilling

import android.content.Context
import ru.wb.perevozka.R
import ru.wb.perevozka.mvvm.BaseMessageResourceProvider
import kotlin.math.abs

class CourierBillingResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getAmount(amount: String): String {
        return context.getString(R.string.courier_orders_details_coast, amount)
    }

    fun getBoxDateAndTime(date: String, time: String): String =
        context.getString(R.string.courier_billing_date_and_time, date, time)

}