package ru.wb.go.ui.couriercarnumber

import ru.wb.go.db.entity.courier.CourierOrderEntity

sealed class CourierCarNumberNavigationState {

    data class NavigateToOrderDetails(
        val title: String,
        val orderNumber: String,
        val order: CourierOrderEntity
    ) : CourierCarNumberNavigationState()

}