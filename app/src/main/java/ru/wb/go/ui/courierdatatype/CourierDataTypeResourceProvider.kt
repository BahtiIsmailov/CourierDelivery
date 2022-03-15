package ru.wb.go.ui.courierdatatype

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider

class CourierDataTypeResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getIp() = context.getString(R.string.courier_data_type_ip_label)
    fun getSelfEmployed() = context.getString(R.string.courier_data_type_self_employed_label)

}