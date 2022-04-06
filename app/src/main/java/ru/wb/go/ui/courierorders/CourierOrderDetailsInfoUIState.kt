package ru.wb.go.ui.courierorders

sealed class CourierOrderDetailsInfoUIState {

    data class InitOrderDetails(
        val carNumber: String,
        val itemId: String,
        val orderId: String,
        val cost: String,
        val cargo: String,
        val countPvz: String,
        val reserve: String,
    ) : CourierOrderDetailsInfoUIState()

}