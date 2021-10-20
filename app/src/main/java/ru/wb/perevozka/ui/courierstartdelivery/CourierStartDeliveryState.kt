package ru.wb.perevozka.ui.courierstartdelivery

sealed class CourierStartDeliveryState {

    data class InfoDelivery(
        val amount: String, val deliveredCount: String
    ) : CourierStartDeliveryState()

}