package ru.wb.go.ui.couriercarnumber

import ru.wb.go.ui.couriercarnumber.keyboard.CarNumberKeyboardMode

sealed class CourierCarNumberUIState {

    object NumberFormatComplete : CourierCarNumberUIState()

    object NumberNotFilled : CourierCarNumberUIState()

    data class NumberSpanFormat(
        val numberFormat: String,
        val numberSpanLength: Int,
        val regionFormat: String,
        val regionSpanLength: Int,
        @CarNumberKeyboardMode val mode: Int
    ) : CourierCarNumberUIState()

    data class InitTypeItems(val items: List<CourierCarTypeItem>) : CourierCarNumberUIState()

    object CloseTypeItems : CourierCarNumberUIState()

    data class SelectedCarType(val item: CourierCarTypeItem) : CourierCarNumberUIState()

}