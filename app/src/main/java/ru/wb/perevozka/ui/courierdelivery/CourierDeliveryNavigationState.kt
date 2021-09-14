package ru.wb.perevozka.ui.courierdelivery

import ru.wb.perevozka.db.entity.courier.CourierOrderEntity

sealed class CourierDeliveryNavigationState {

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierDeliveryNavigationState()

    data class NavigateToDialogConfirm(
        val title: String, val message: String
    ) : CourierDeliveryNavigationState()

    data class NavigateToCarNumber(val title: String, val order: CourierOrderEntity) :
        CourierDeliveryNavigationState()

    object NavigateToOrderConfirm : CourierDeliveryNavigationState()

}
