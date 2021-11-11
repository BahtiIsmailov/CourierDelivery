package ru.wb.go.ui.courierunloading

sealed class CourierUnloadingScanNavAction {

    object NavigateToUnknownBox : CourierUnloadingScanNavAction()

    object NavigateToBoxes : CourierUnloadingScanNavAction()

    data class  NavigateToConfirmDialog(
        val title: String,
        val message: String,
    ) : CourierUnloadingScanNavAction()

    object NavigateToIntransit : CourierUnloadingScanNavAction()

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierUnloadingScanNavAction()

}