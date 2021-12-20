package ru.wb.go.ui.courierintransit

sealed class CourierIntransitNavigationState {

    object NavigateToScanner : CourierIntransitNavigationState()

    object NavigateToMap : CourierIntransitNavigationState()

    data class NavigateToUnloadingScanner(val officeId: Int) : CourierIntransitNavigationState()

    data class NavigateToCompleteDelivery(
        val amount: Int,
        val unloadedCount: Int,
        val fromCount: Int
    ) : CourierIntransitNavigationState()

}
