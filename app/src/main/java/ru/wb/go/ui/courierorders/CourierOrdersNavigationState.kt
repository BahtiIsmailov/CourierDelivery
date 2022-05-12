package ru.wb.go.ui.courierorders

import ru.wb.go.ui.couriercarnumber.CourierCarNumberResult

sealed class CourierOrdersNavigationState {

    data class NavigateToCarNumber(val result: CourierCarNumberResult) :
        CourierOrdersNavigationState()

    object NavigateToRegistration : CourierOrdersNavigationState()

    object NavigateToWarehouse : CourierOrdersNavigationState()

    object CourierLoader : CourierOrdersNavigationState()

    object NavigateToOrders : CourierOrdersNavigationState()

    data class NavigateToOrderDetails(val isDemo: Boolean) : CourierOrdersNavigationState()

    object NavigateToAddresses : CourierOrdersNavigationState()

    object CloseAddressesDetail : CourierOrdersNavigationState()

    data class ShowAddressDetail(val address: String) : CourierOrdersNavigationState()

    object NavigateToRegistrationDialog : CourierOrdersNavigationState()

    object NavigateToTimer : CourierOrdersNavigationState()

    object OnMapClick : CourierOrdersNavigationState()

}
