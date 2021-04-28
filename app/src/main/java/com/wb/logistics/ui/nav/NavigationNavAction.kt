package com.wb.logistics.ui.nav

sealed class NavigationNavAction {

    object NavigateToFlight : NavigationNavAction()
    object NavigateToReceptionScan : NavigationNavAction()
    object NavigateToPickUpPoint : NavigationNavAction()
    object NavigateToDelivery : NavigationNavAction()

}
