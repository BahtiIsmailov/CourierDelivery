package ru.wb.perevozka.ui.courierorderconferm

sealed class CourierOrderConfirmInfoUIState {

    data class InitOrderInfo(
        val order: String,
        val coast: String,
        val carNumber: String,
        val arrive: String,
        val volume: String,
    ) : CourierOrderConfirmInfoUIState()

}