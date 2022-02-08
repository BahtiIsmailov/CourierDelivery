package ru.wb.go.ui.couriercarnumber

import ru.wb.go.ui.couriercarnumber.keyboard.CarNumberKeyboardMode

sealed class CourierCarNumberUIState {

    object NumberFormatComplete : CourierCarNumberUIState()
    object NumberNotFilled : CourierCarNumberUIState()

    data class NumberSpanFormat(
        val numberFormat: String,
        val count: Int,
        @CarNumberKeyboardMode val mode: Int
    ) : CourierCarNumberUIState()

}