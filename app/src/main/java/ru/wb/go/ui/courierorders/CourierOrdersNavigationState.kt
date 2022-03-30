package ru.wb.go.ui.courierorders

sealed class CourierOrdersNavigationState {

//    data class NavigateToOrderDetails(
//        val title: String,
//        val orderNumber: String,
//        val order: CourierOrderEntity,
//        val warehouseLatitude: Double,
//        val warehouseLongitude: Double
//    ) : CourierOrdersNavigationState()

    object NavigateToOrderDetails : CourierOrdersNavigationState()

    data class NavigateToCarNumber(
        val isEdit: Boolean
//        val title: String,
//        val orderNumber: String,
//        val order: CourierOrderEntity,
//        val warehouseLatitude: Double,
//        val warehouseLongitude: Double
    ) : CourierOrdersNavigationState()

    object NavigateToRegistration : CourierOrdersNavigationState()

    object NavigateToWarehouse : CourierOrdersNavigationState()

    object NavigateToOrders : CourierOrdersNavigationState()

    object NavigateToRegistrationDialog: CourierOrdersNavigationState()


}
