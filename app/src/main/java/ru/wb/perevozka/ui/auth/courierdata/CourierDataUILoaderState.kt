package ru.wb.perevozka.ui.auth.courierdata

sealed class CourierDataUILoaderState {
    object Progress : CourierDataUILoaderState()
    object Enable : CourierDataUILoaderState()
    object Disable : CourierDataUILoaderState()
}