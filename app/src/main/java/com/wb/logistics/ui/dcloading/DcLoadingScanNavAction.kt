package com.wb.logistics.ui.dcloading

sealed class DcLoadingScanNavAction {
    data class NavigateToReceptionBoxNotBelong(
        val toolbarTitle: String,
        val title: String,
        val box: String,
        val address: String,
    ) :
        DcLoadingScanNavAction()

    object NavigateToBoxes : DcLoadingScanNavAction()

    object NavigateToFlightDeliveries : DcLoadingScanNavAction()

    object NavigateToBack : DcLoadingScanNavAction()

}