package ru.wb.perevozka.ui.courierdata

sealed class CourierDataUILoaderState {
    object Progress : CourierDataUILoaderState()
    object Enable : CourierDataUILoaderState()
    object Disable : CourierDataUILoaderState()
}