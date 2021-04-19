package com.wb.logistics.ui.flightdeliveries

sealed class FlightDeliveriesUIAction {

    object Refresh : FlightDeliveriesUIAction()
    object GoToDeliveryClick : FlightDeliveriesUIAction()
    object GoToDeliveryConfirmClick : FlightDeliveriesUIAction()

}