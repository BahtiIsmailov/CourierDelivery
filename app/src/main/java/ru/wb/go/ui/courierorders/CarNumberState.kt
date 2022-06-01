package ru.wb.go.ui.courierorders

sealed class CarNumberState {

    data class Indicated(val carNumber: String) : CarNumberState()

    object Empty : CarNumberState()

}
