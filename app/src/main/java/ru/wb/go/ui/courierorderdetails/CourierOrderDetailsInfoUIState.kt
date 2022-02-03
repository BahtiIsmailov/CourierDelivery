package ru.wb.go.ui.courierorderdetails

sealed class CourierOrderDetailsInfoUIState {

    data class InitOrderInfo(
        val carNumber: String,
        val orderNumber: String,
        val order: String,
        val coast: String,
        val countBox: String,
        val volume: String,
        val countPvz: String,
        val arrive: String,
    ) : CourierOrderDetailsInfoUIState()

}