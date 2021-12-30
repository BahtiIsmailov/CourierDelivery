package ru.wb.go.ui.courierbilllingcomplete

sealed class CourierBillingCompleteState {

    data class InfoDelivery(val title: String) : CourierBillingCompleteState()

}