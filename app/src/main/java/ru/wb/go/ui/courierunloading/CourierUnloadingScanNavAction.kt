package ru.wb.go.ui.courierunloading

sealed class CourierUnloadingScanNavAction {

    data class
    NavigateToBoxes(val officeId: Int) : CourierUnloadingScanNavAction()

    object NavigateToIntransit : CourierUnloadingScanNavAction()

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierUnloadingScanNavAction()

}