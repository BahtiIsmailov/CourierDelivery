package ru.wb.perevozka.ui.courierdelivery

sealed class CourierDeliveryInfoUIState {

    data class InitOrderInfo(
        val order: String,
        val coast: String,
        val countBoxAndVolume: String,
        val countPvz: String,
    ) : CourierDeliveryInfoUIState()

}