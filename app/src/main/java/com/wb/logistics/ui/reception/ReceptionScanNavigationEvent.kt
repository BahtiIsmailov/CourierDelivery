package com.wb.logistics.ui.reception

sealed class ReceptionScanNavigationEvent {
    data class NavigateToReceptionBoxNotBelong(
        val toolbarTitle: String,
        val title: String,
        val box: String,
        val address: String,
    ) :
        ReceptionScanNavigationEvent()

    object NavigateToBoxes : ReceptionScanNavigationEvent()

    object NavigateToFlightDeliveries : ReceptionScanNavigationEvent()

    object NavigateToBack : ReceptionScanNavigationEvent()

}