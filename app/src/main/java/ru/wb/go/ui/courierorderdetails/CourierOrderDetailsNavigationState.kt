package ru.wb.go.ui.courierorderdetails

import ru.wb.go.db.entity.courier.CourierOrderEntity

sealed class CourierOrderDetailsNavigationState {

    data class NavigateToDialogConfirm(
        val title: String, val message: String
    ) : CourierOrderDetailsNavigationState()

    data class NavigateToCarNumber(val title: String, val order: CourierOrderEntity) :
        CourierOrderDetailsNavigationState()

    object NavigateToOrderConfirm : CourierOrderDetailsNavigationState()

}