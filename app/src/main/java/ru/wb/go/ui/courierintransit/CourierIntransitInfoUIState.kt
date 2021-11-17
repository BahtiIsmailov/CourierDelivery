package ru.wb.go.ui.courierintransit

sealed class CourierIntransitInfoUIState {

    data class InitOrderInfo(
        val order: String,
        val coast: String,
        val countBoxAndVolume: String,
        val countPvz: String,
    ) : CourierIntransitInfoUIState()

}