package ru.wb.go.ui.courierbillingaccountselector

sealed class CourierBillingAccountSelectorUIState {
    data class Empty(val message: String, val typeBillingAccount: CourierBillingAccountSelectorQueryType) : CourierBillingAccountSelectorUIState()
    data class Error(val formatBalance: String, val message: String, val typeBillingAccount: CourierBillingAccountSelectorQueryType) : CourierBillingAccountSelectorUIState()
    data class ErrorFocus(val message: String, val typeBillingAccount: CourierBillingAccountSelectorQueryType) : CourierBillingAccountSelectorUIState()
    data class Complete(val formatBalance: String, val typeBillingAccount: CourierBillingAccountSelectorQueryType) : CourierBillingAccountSelectorUIState()
    object NextComplete : CourierBillingAccountSelectorUIState()
}