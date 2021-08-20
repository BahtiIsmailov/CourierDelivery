package ru.wb.perevozka.ui.courierorders

import ru.wb.perevozka.mvvm.model.base.BaseItem
import ru.wb.perevozka.ui.courierwarehouses.CourierWarehousesUINavState

sealed class CourierOrderUIListState {

    data class ShowOrders(val items: List<BaseItem>) : CourierOrderUIListState()

    data class NavigateToMessageInfo(val message: String, val button: String) : CourierOrderUIListState()

    data class Empty(val info: String) : CourierOrderUIListState()

}
