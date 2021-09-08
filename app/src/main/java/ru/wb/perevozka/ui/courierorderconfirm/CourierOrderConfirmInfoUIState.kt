package ru.wb.perevozka.ui.courierorderconfirm

sealed class CourierOrderConfirmInfoUIState {

    data class InitOrderInfo(
        val order: String,
        val coast: String,
        val carNumber: String,
        val arrive: String,
        val volume: String,
    ) : CourierOrderConfirmInfoUIState()

}