package ru.wb.go.ui.courierorderdetails

sealed class CourierOrderDetailsInfoUIState {

    data class InitOrderInfo(
        val lineNumber: String,
        val orderId: String,
        val cost: String,
        val cargo: String,
        val countPvz: String,
        val reserve: String,
    ) : CourierOrderDetailsInfoUIState()

    data class NumberSpanFormat(val numberFormat: String) : CourierOrderDetailsInfoUIState()

}