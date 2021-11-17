package ru.wb.go.ui.courierexpects

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider

class CourierExpectsResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun notConfirmDataTitle() = context.getString(R.string.courier_expects_title)
    fun notConfirmDataMessage() = context.getString(R.string.courier_expects_message)
    fun notConfirmDataPositive() = context.getString(R.string.courier_expects_positive)

}