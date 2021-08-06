package ru.wb.perevozka.ui.flightpickpoint

sealed class FlightPickPointUIAction {

    object GoToDeliveryClick : FlightPickPointUIAction()
    object GoToDeliveryConfirmClick : FlightPickPointUIAction()

}