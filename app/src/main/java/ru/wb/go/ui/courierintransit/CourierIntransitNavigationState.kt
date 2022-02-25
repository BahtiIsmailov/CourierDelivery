package ru.wb.go.ui.courierintransit

sealed class CourierIntransitNavigationState {

    object NavigateToScanner : CourierIntransitNavigationState()

    data class NavigateToCompleteDelivery(
        val amount: Int,
        val unloadedCount: Int,
        val fromCount: Int
    ) : CourierIntransitNavigationState()

}
