package ru.wb.perevozka.ui.couriercarnumber

sealed class CourierCarNumberBackspaceUIState {
    object Inactive : CourierCarNumberBackspaceUIState()
    object Active : CourierCarNumberBackspaceUIState()
}