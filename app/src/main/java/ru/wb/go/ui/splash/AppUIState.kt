package ru.wb.go.ui.splash

sealed class AppUIState {

    data class NotAssigned(val delivery: String) : AppUIState()
    data class Loading(val deliveryId: String) : AppUIState()
    data class InTransit(val deliveryId: String) : AppUIState()

}