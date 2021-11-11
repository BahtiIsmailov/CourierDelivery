package ru.wb.perevozka.ui.courierorders

import ru.wb.perevozka.db.entity.courier.CourierOrderEntity

sealed class CourierOrdersNavigationState {

    data class NavigateToOrderDetails(val title: String, val order: CourierOrderEntity) :
        CourierOrdersNavigationState()

}
