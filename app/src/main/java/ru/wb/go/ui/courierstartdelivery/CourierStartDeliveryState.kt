package ru.wb.go.ui.courierstartdelivery

sealed class CourierStartDeliveryState {

    data class InfoDelivery(
        val amount: String, val deliveredCount: String
    ) : CourierStartDeliveryState()

}