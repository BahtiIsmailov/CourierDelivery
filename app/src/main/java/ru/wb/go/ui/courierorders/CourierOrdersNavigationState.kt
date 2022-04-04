package ru.wb.go.ui.courierorders

sealed class CourierOrdersNavigationState {

    data class NavigateToCarNumber(val id: Int) : CourierOrdersNavigationState()

    object NavigateToRegistration : CourierOrdersNavigationState()

    object NavigateToWarehouse : CourierOrdersNavigationState()

    object NavigateToOrders : CourierOrdersNavigationState()

    data class  NavigateToOrderDetails(val isDemo: Boolean) : CourierOrdersNavigationState()

    object NavigateToAddresses : CourierOrdersNavigationState()

    object NavigateToBack : CourierOrdersNavigationState()

    object NavigateToRegistrationDialog : CourierOrdersNavigationState()

    object NavigateToTimer : CourierOrdersNavigationState()

}
