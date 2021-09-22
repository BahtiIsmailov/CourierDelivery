package ru.wb.perevozka.ui.courierintransit

sealed class CourierIntransitNavigationState {

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierIntransitNavigationState()

    data class NavigateToDialogConfirm(
        val title: String, val message: String
    ) : CourierIntransitNavigationState()

    object NavigateToScanner : CourierIntransitNavigationState()

    object NavigateToMap : CourierIntransitNavigationState()

    data class NavigateToUnloadingScanner(val officeId: Int) : CourierIntransitNavigationState()

    object NavigateToCompleteDelivery : CourierIntransitNavigationState()

}
