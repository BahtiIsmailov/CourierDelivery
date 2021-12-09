package ru.wb.go.ui.couriercompletedelivery

sealed class CourierCompleteDeliveryState {

    data class InfoDelivery(
        val amount: String, val deliveredCount: String
    ) : CourierCompleteDeliveryState()

}