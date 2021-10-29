package ru.wb.perevozka.ui.courierbillingaccountselector

sealed class CourierBillingAccountSelectorUIState {
    data class Error(val message: String, val typeBillingAccount: CourierBillingAccountSelectorQueryType) : CourierBillingAccountSelectorUIState()
    data class ErrorFocus(val message: String, val typeBillingAccount: CourierBillingAccountSelectorQueryType) : CourierBillingAccountSelectorUIState()
    data class Complete(val format: String, val typeBillingAccount: CourierBillingAccountSelectorQueryType) : CourierBillingAccountSelectorUIState()
    object Next : CourierBillingAccountSelectorUIState()
}