package ru.wb.perevozka.ui.courierbillingaccountselector

sealed class CourierBillingAccountSelectorAdapterItem {

    data class Edit(val text: String) : CourierBillingAccountSelectorAdapterItem()

    data class Add(val text: String) : CourierBillingAccountSelectorAdapterItem()
}