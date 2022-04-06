package ru.wb.go.ui.courierorders

import ru.wb.go.ui.couriercarnumber.CourierCarNumberResult

sealed class CourierOrdersNavigationState {

    data class NavigateToCarNumber(val result: CourierCarNumberResult) :
        CourierOrdersNavigationState()

    object NavigateToRegistration : CourierOrdersNavigationState()

    object NavigateToWarehouse : CourierOrdersNavigationState()

    object NavigateToOrders : CourierOrdersNavigationState()

    data class NavigateToOrderDetails(val isDemo: Boolean) : CourierOrdersNavigationState()

    object NavigateToAddresses : CourierOrdersNavigationState()

    object NavigateToRegistrationDialog : CourierOrdersNavigationState()

    object NavigateToTimer : CourierOrdersNavigationState()

}
