package ru.wb.go.ui.courierbilling

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider
import kotlin.math.abs

class CourierBillingResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getTitle(): String {
        return context.getString(R.string.courier_billing_title)
    }

    fun getAmount(amount: String): String {
        return context.getString(R.string.courier_orders_details_coast, amount)
    }

    fun getBillingTime(time: String) =
        context.getString(R.string.courier_billing_date_and_time, time)

    fun getEmptyList(): String {
        return context.getString(R.string.courier_billing_empty_list)
    }

    fun getPositiveAmount(amount: String): String {
        return context.getString(R.string.courier_billing_positive_amount, amount)
    }

    fun getNegativeAmount(amount: String): String {
        return context.getString(R.string.courier_billing_negative_amount, amount)
    }

}