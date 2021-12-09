package ru.wb.go.ui.courierbillingaccountdata

sealed class CourierBillingAccountDataUILoaderState {
    object Progress : CourierBillingAccountDataUILoaderState()
    object Enable : CourierBillingAccountDataUILoaderState()
    object Disable : CourierBillingAccountDataUILoaderState()
}