package ru.wb.perevozka.ui.courierordertimer

sealed class CourierOrderTimerInfoUIState {

    data class InitOrderInfo(
        val order: String,
        val name: String,
        val coast: String,
        val countBoxAndVolume: String,
        val countPvz: String,
    ) : CourierOrderTimerInfoUIState()

}