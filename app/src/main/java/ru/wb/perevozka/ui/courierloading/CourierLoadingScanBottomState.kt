package ru.wb.perevozka.ui.courierloading

sealed class CourierLoadingScanBottomState {

    object Enable : CourierLoadingScanBottomState()
    object Progress : CourierLoadingScanBottomState()
    object Disable : CourierLoadingScanBottomState()

}