package ru.wb.perevozka.ui.auth.courierexpects

import android.content.Context
import ru.wb.perevozka.R
import ru.wb.perevozka.mvvm.BaseMessageResourceProvider

class CourierExpectsResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun notConfirmDataTitle() = context.getString(R.string.courier_expects_title)
    fun notConfirmDataMessage() = context.getString(R.string.courier_expects_message)
    fun notConfirmDataPositive() = context.getString(R.string.courier_expects_positive)

}