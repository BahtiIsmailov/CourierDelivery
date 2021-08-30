package ru.wb.perevozka.ui.courierloading

sealed class CourierLoadingScanToastState {

    data class BoxAdded(val message: String) : CourierLoadingScanToastState()
    data class BoxHasBeenAdded(val message: String) : CourierLoadingScanToastState()

}