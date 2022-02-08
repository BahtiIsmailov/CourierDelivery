package ru.wb.go.ui.courierorders

import ru.wb.go.db.entity.courier.CourierOrderEntity

sealed class CourierOrdersNavigationState {

    data class NavigateToOrderDetails(
        val title: String,
        val orderNumber: String,
        val order: CourierOrderEntity,
        val warehouseLatitude: Double,
        val warehouseLongitude: Double
    ) : CourierOrdersNavigationState()

    data class NavigateToCarNumber(
        val title: String,
        val orderNumber: String,
        val order: CourierOrderEntity,
        val warehouseLatitude: Double,
        val warehouseLongitude: Double
    ) : CourierOrdersNavigationState()

    object NavigateToRegistration : CourierOrdersNavigationState()

}
