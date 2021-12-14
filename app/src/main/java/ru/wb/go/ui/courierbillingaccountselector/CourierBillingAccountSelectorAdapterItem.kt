package ru.wb.go.ui.courierbillingaccountselector

sealed class CourierBillingAccountSelectorAdapterItem {

    data class Edit(val text: String, val shortText: String) : CourierBillingAccountSelectorAdapterItem()

    data class Add(val text: String) : CourierBillingAccountSelectorAdapterItem()
}