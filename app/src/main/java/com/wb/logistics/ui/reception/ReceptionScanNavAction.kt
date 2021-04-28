package com.wb.logistics.ui.reception

sealed class ReceptionScanNavAction {
    data class NavigateToReceptionBoxNotBelong(
        val toolbarTitle: String,
        val title: String,
        val box: String,
        val address: String,
    ) :
        ReceptionScanNavAction()

    object NavigateToBoxes : ReceptionScanNavAction()

    object NavigateToFlightDeliveries : ReceptionScanNavAction()

    object NavigateToBack : ReceptionScanNavAction()

}