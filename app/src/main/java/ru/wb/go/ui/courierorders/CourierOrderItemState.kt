package ru.wb.go.ui.courierorders

import ru.wb.go.mvvm.model.base.BaseItem

sealed class CourierOrderItemState {

    data class ShowOrders(val items: List<BaseItem>) : CourierOrderItemState()

    data class Empty(val info: String) : CourierOrderItemState()

}
