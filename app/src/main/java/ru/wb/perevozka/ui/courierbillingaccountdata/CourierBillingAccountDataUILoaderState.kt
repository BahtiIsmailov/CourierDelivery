package ru.wb.perevozka.ui.courierbillingaccountdata

sealed class CourierBillingAccountDataUILoaderState {
    object Progress : CourierBillingAccountDataUILoaderState()
    object Enable : CourierBillingAccountDataUILoaderState()
    object Disable : CourierBillingAccountDataUILoaderState()
}