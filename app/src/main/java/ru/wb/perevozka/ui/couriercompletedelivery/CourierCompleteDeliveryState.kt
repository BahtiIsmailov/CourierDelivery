package ru.wb.perevozka.ui.couriercompletedelivery

sealed class CourierCompleteDeliveryState {

    data class InfoDelivery(
        val amount: String, val deliveredCount: String
    ) : CourierCompleteDeliveryState()

}