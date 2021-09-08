package ru.wb.perevozka.ui.courierorderconfirm

sealed class CourierOrderConfirmState {

    data class Timer(val timeAnalog: Float, val timeDigit: String) : CourierOrderConfirmState()

}