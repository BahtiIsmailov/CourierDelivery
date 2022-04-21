package ru.wb.go.ui.courierdatatype

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseServicesResourceProvider

class CourierDataTypeResourceProvider(private val context: Context) :
    BaseServicesResourceProvider(context) {

    fun getIp() = context.getString(R.string.courier_data_type_ip)

    fun getSelfEmployed() = context.getString(R.string.courier_data_type_self_employed)

}