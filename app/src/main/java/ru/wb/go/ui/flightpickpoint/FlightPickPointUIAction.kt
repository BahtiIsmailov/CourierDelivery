package ru.wb.go.ui.flightpickpoint

sealed class FlightPickPointUIAction {

    object GoToDeliveryClick : FlightPickPointUIAction()
    object GoToDeliveryConfirmClick : FlightPickPointUIAction()

}