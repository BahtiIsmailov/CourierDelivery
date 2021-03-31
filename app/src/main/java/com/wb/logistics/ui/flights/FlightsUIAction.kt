package com.wb.logistics.ui.flights

sealed class FlightsUIAction {

    object Refresh : FlightsUIAction()
    object NetworkInfoClick : FlightsUIAction()
    object ReceptionBoxesClick : FlightsUIAction()
    object ReturnToBalanceClick : FlightsUIAction()
    object ContinueAcceptanceClick : FlightsUIAction()
    object RemoveBoxesClick : FlightsUIAction()

}