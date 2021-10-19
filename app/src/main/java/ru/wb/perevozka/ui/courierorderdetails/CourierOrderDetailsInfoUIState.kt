package ru.wb.perevozka.ui.courierorderdetails

sealed class CourierOrderDetailsInfoUIState {

    data class InitOrderInfo(
        val order: String,
        val coast: String,
        val arrive: String,
        val countBoxAndVolume: String,
        val countPvz: String,
    ) : CourierOrderDetailsInfoUIState()

}