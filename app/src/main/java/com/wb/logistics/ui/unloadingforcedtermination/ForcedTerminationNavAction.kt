package com.wb.logistics.ui.unloadingforcedtermination

sealed class ForcedTerminationNavAction {

    object NavigateToFlightDeliveries : ForcedTerminationNavAction()

    object NavigateToBack : ForcedTerminationNavAction()

}