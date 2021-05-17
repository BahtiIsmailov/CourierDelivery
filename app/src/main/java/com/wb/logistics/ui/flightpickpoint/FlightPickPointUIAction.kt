package com.wb.logistics.ui.flightpickpoint

sealed class FlightPickPointUIAction {

    object GoToDeliveryClick : FlightPickPointUIAction()
    object GoToDeliveryConfirmClick : FlightPickPointUIAction()

}