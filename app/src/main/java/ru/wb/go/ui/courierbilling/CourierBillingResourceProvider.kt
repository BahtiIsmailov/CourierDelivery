package ru.wb.go.ui.courierbilling

import android.content.Context

import ru.wb.go.R
import ru.wb.go.mvvm.BaseServicesResourceProvider
import java.text.NumberFormat

class CourierBillingResourceProvider(private val context: Context) :
    BaseServicesResourceProvider(context) {

    fun getTitle(): String {
        return context.getString(R.string.courier_billing_title)
    }

    fun getBillingTime(time: String) =
        context.getString(R.string.courier_billing_date_and_time, time)

    fun getEmptyList(): String {
        return context.getString(R.string.courier_billing_empty_list)
    }

    fun formatMoney(amount:Int, needSign: Boolean):String{
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 0
        var sign = ""

        if(needSign) {
            (if (amount < 0) {
                "-"
            }else{
                "+"
            }).also { sign = it }
        }

       return "$sign ${format.format(amount)}"

    }

    fun iconIsRejected(): Int {
        return R.drawable.ic_billing_is_rejected
    }

    fun iconIsProcessing(): Int {
        return R.drawable.ic_billing_is_processing
    }

}