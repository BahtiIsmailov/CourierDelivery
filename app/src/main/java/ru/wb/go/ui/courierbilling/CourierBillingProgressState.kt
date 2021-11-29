package ru.wb.go.ui.courierbilling

sealed class CourierBillingProgressState {
    object Progress : CourierBillingProgressState()
    object Complete : CourierBillingProgressState()
}
