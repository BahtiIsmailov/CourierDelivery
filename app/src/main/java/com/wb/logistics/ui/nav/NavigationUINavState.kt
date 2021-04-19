package com.wb.logistics.ui.nav

sealed class NavigationUINavState {

    object NavigateToFlight : NavigationUINavState()
    object NavigateToReceptionScan : NavigationUINavState()
    object NavigateToPickUpPoint : NavigationUINavState()
    object NavigateToDelivery : NavigationUINavState()

}
