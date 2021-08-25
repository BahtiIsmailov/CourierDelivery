package ru.wb.perevozka.ui.couriercarnumber

import ru.wb.perevozka.ui.couriercarnumber.keyboard.CarNumberKeyboardMode

sealed class CourierCarNumberUIState {

    object NumberFormatComplete : CourierCarNumberUIState()
    object NumberNotFilled : CourierCarNumberUIState()

    data class NumberSpanFormat(
        val numberFormat: String,
        val count: Int,
        @CarNumberKeyboardMode val mode: Int
    ) : CourierCarNumberUIState()

    data class NumberNotFound(
        val title: String,
        val message: String,
        val button: String
    ) : CourierCarNumberUIState()

    data class Error(val title: String, val message: String, val button: String) :
        CourierCarNumberUIState()
}