package ru.wb.perevozka.ui.courierbilling

sealed class CourierBillingProgressState {
    object Progress : CourierBillingProgressState()
    object Complete : CourierBillingProgressState()
}
