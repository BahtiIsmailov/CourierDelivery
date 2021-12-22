package ru.wb.go.ui.courierbillingaccountselector

sealed class CourierBillingAccountSelectorAdapterItem {

    data class Edit(val bankName: String, val lastFourAccount: String) : CourierBillingAccountSelectorAdapterItem()

    data class Add(val text: String) : CourierBillingAccountSelectorAdapterItem()
}