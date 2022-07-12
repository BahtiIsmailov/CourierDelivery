package ru.wb.go.ui.courierordertimer

sealed class CourierOrderTimerInfoUIState {

    data class InitOrderInfo(
        val route: String,
        val order: String,
        val name: String,
        val coast: String,
        val countBoxAndVolume: String,
        val countPvz: String,
        val gate: String,
    ) : CourierOrderTimerInfoUIState()

}