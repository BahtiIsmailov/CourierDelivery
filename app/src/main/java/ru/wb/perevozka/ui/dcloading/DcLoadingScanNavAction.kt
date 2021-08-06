package ru.wb.perevozka.ui.dcloading

sealed class DcLoadingScanNavAction {
    data class NavigateToReceptionBoxNotBelong(
        val title: String,
        val box: String,
        val address: String,
    ) :
        DcLoadingScanNavAction()

    object NavigateToBoxes : DcLoadingScanNavAction()

    object NavigateToHandle : DcLoadingScanNavAction()

    object NavigateToFlightDeliveries : DcLoadingScanNavAction()

    object NavigateToBack : DcLoadingScanNavAction()

}