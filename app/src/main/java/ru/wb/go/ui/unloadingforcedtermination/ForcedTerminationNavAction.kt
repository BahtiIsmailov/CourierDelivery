package ru.wb.go.ui.unloadingforcedtermination

sealed class ForcedTerminationNavAction {

    object NavigateToFlightDeliveries : ForcedTerminationNavAction()

    object NavigateToBack : ForcedTerminationNavAction()

}