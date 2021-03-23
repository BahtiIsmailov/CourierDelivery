package com.wb.logistics.ui.flights

sealed class FlightsPasswordUIAction {

    object Refresh : FlightsPasswordUIAction()
    object NetworkInfoClick : FlightsPasswordUIAction()
    object ReceptionBoxesClick : FlightsPasswordUIAction()
    object ReturnToBalanceClick : FlightsPasswordUIAction()
    object ContinueAcceptanceClick : FlightsPasswordUIAction()

}