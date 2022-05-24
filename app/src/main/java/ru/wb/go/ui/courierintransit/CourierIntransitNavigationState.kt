package ru.wb.go.ui.courierintransit

sealed class CourierIntransitNavigationState {

    data class NavigateToNavigator(
        val latitude: Double,
        val longitude: Double,
    ) : CourierIntransitNavigationState()

    object NavigateToScanner : CourierIntransitNavigationState()

    data class NavigateToCompleteDelivery(
        val amount: Int,
        val unloadedCount: Int,
        val fromCount: Int
    ) : CourierIntransitNavigationState()

}
