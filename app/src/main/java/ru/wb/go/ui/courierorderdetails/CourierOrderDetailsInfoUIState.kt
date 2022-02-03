package ru.wb.go.ui.courierorderdetails

sealed class CourierOrderDetailsInfoUIState {

    data class InitOrderInfo(
        val orderNumber: String,
        val order: String,
        val coast: String,
        val countBox: String,
        val volume: String,
        val countPvz: String,
        val arrive: String,
    ) : CourierOrderDetailsInfoUIState()

    data class NumberSpanFormat(val numberFormat: String) : CourierOrderDetailsInfoUIState()

}