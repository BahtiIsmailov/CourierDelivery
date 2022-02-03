package ru.wb.go.ui.courierbillingaccountdata

sealed class CourierBillingAccountDataUIState {
    data class Error(val message: String, val typeBillingAccount: CourierBillingAccountDataQueryType) : CourierBillingAccountDataUIState()
    data class ErrorFocus(val message: String, val typeBillingAccount: CourierBillingAccountDataQueryType) : CourierBillingAccountDataUIState()
    data class Complete(val format: String, val typeBillingAccount: CourierBillingAccountDataQueryType) : CourierBillingAccountDataUIState()
    object Next : CourierBillingAccountDataUIState()
}