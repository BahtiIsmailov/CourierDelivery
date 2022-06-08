package ru.wb.go.ui.courierorders

import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.courierorders.delegates.items.CourierOrderItem

sealed class CourierOrderItemState {

    data class ShowItems(val items: List<BaseItem>) : CourierOrderItemState()

    data class UpdateItems(val items: MutableList<BaseItem>) : CourierOrderItemState()

    data class Empty(val info: String) : CourierOrderItemState()

    data class UpdateItem(val position: Int, val item: CourierOrderItem) : CourierOrderItemState()

}
