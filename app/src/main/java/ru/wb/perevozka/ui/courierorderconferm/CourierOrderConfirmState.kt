package ru.wb.perevozka.ui.courierorderconferm

sealed class CourierOrderConfirmState {

    data class Timer(val timeAnalog: Float, val timeDigit: String) : CourierOrderConfirmState()

}