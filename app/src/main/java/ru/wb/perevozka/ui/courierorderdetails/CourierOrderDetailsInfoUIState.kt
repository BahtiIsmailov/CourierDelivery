package ru.wb.perevozka.ui.courierorderdetails

sealed class CourierOrderDetailsInfoUIState {

    data class InitOrderInfo(
        val order: String,
        val coast: String,
        val countBoxAndVolume: String,
        val countPvz: String,
        val routesTitle: String
    ) : CourierOrderDetailsInfoUIState()

}