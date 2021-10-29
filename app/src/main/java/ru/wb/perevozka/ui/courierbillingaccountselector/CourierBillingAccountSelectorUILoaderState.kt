package ru.wb.perevozka.ui.courierbillingaccountselector

sealed class CourierBillingAccountSelectorUILoaderState {
    object Progress : CourierBillingAccountSelectorUILoaderState()
    object Enable : CourierBillingAccountSelectorUILoaderState()
    object Disable : CourierBillingAccountSelectorUILoaderState()
}