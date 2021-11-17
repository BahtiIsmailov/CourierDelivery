package ru.wb.go.ui.couriercarnumber

sealed class CourierCarNumberBackspaceUIState {
    object Inactive : CourierCarNumberBackspaceUIState()
    object Active : CourierCarNumberBackspaceUIState()
}