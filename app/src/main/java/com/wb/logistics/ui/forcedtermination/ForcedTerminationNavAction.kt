package com.wb.logistics.ui.forcedtermination

sealed class ForcedTerminationNavAction {

    object NavigateToFlightDeliveries : ForcedTerminationNavAction()

    object NavigateToBack : ForcedTerminationNavAction()

}