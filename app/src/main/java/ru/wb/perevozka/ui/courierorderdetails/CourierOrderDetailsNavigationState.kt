package ru.wb.perevozka.ui.courierorderdetails

import ru.wb.perevozka.db.entity.courier.CourierOrderEntity

sealed class CourierOrderDetailsNavigationState {

    data class NavigateToDialogConfirm(
        val title: String, val message: String
    ) : CourierOrderDetailsNavigationState()

    data class NavigateToCarNumber(val title: String, val order: CourierOrderEntity) :
        CourierOrderDetailsNavigationState()

    object NavigateToOrderConfirm : CourierOrderDetailsNavigationState()

}
