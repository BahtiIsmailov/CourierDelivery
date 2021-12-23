package ru.wb.go.ui.splash

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider

class AppResourceProvider(private val context: Context) : BaseMessageResourceProvider(context) {

    fun getCount(count: Int) = context.getString(R.string.count_boxes, count)
    fun getDeliveryId(id: String) = context.getString(R.string.delivery, id)

}