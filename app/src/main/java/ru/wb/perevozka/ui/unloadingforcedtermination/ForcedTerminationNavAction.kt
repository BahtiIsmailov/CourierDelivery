package ru.wb.perevozka.ui.unloadingforcedtermination

sealed class ForcedTerminationNavAction {

    object NavigateToFlightDeliveries : ForcedTerminationNavAction()

    object NavigateToBack : ForcedTerminationNavAction()

}