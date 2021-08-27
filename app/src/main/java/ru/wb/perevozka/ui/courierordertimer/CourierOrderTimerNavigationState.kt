package ru.wb.perevozka.ui.courierordertimer

sealed class CourierOrderTimerNavigationState {

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierOrderTimerNavigationState()

    data class NavigateToDialogConfirm(
        val title: String, val message: String
    ) : CourierOrderTimerNavigationState()

    object NavigateToCarNumber: CourierOrderTimerNavigationState()

}
