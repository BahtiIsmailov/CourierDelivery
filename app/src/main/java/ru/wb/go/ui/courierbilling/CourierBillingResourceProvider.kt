package ru.wb.go.ui.courierbilling

import android.content.Context

import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider
import java.text.NumberFormat
import java.util.*

class CourierBillingResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

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
        format.maximumFractionDigits = 2
        var sign = ""
        if(needSign) {

            if (amount < 0) {
                sign = "-"
            }else{
                sign="+"
            }
        }
       return  sign+format.format(amount)

    }

}