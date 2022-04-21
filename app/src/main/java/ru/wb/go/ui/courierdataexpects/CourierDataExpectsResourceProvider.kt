package ru.wb.go.ui.courierdataexpects

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseServicesResourceProvider

class CourierDataExpectsResourceProvider(private val context: Context) :
    BaseServicesResourceProvider(context) {

    fun notConfirmDataTitle() = context.getString(R.string.attention_title)
    fun notConfirmDataMessage() = context.getString(R.string.courier_expects_message)
    fun notConfirmDataPositive() = context.getString(R.string.ok_button_title)

}