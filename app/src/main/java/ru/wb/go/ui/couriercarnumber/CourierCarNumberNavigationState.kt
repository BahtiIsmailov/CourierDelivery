package ru.wb.go.ui.couriercarnumber

sealed class CourierCarNumberNavigationState {

    data class NavigateToOrderDetails(
        val isEdit: Boolean
//        val title: String,
//        val orderNumber: String,
//        val order: CourierOrderEntity,
//        val warehouseLatitude: Double,
//        val warehouseLongitude: Double,
    ) : CourierCarNumberNavigationState()

}