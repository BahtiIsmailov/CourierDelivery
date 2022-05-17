package ru.wb.go.ui.couriercarnumber

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseServicesResourceProvider

class CourierCarNumberResourceProvider(val context: Context) :
    BaseServicesResourceProvider(context) {

    fun getGazeleIcon() = R.drawable.ic_type_gazele
    fun getGazeleName(): String = context.getString(R.string.courier_car_gazele_name)

    fun getWagonIcon() = R.drawable.ic_type_wagon
    fun getWagonName(): String = context.getString(R.string.courier_car_wagon_name)

    fun getStationWagonIcon() = R.drawable.ic_type_station_wagon
    fun getStationWagonName(): String = context.getString(R.string.courier_car_station_wagon_name)

}