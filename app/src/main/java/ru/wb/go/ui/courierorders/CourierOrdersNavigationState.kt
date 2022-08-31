package ru.wb.go.ui.courierorders

import androidx.annotation.DrawableRes
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

    data class ShowAddressDetail(
        @DrawableRes val icon: Int,
        val address: String,
        val workTime: String
    ) :
        CourierOrdersNavigationState()

    object NavigateToRegistrationDialog : CourierOrdersNavigationState()

    object NavigateToTimer : CourierOrdersNavigationState()

    object OnMapClick : CourierOrdersNavigationState()

    object HideOrderDetailsByClickMap : CourierOrdersNavigationState()

}
