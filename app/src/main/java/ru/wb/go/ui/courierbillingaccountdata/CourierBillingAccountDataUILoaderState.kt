package ru.wb.go.ui.courierbillingaccountdata

sealed class CourierBillingAccountDataUILoaderState {
    object Enable : CourierBillingAccountDataUILoaderState()
    object Disable : CourierBillingAccountDataUILoaderState()
}