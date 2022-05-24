package ru.wb.go.ui.couriercarnumber

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseServicesResourceProvider

class CourierCarNumberResourceProvider(val context: Context) :
    BaseServicesResourceProvider(context) {

    fun getTypeIcons() = context.resources.obtainTypedArray(R.array.car_type_icon)

    fun getTypeNames() = context.resources.getStringArray(R.array.car_type_name)

}