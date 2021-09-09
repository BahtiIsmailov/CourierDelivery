package ru.wb.perevozka.ui.courierorderconfirm

sealed class CourierOrderConfirmInfoUIState {

    data class InitOrderInfo(
        val order: String,
        val carNumber: String,
        val arrive: String,
        val pvz: String,
        val volume: String,
        val coast: String,
    ) : CourierOrderConfirmInfoUIState()

}