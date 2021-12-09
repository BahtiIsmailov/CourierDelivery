package ru.wb.go.ui.courierorders

import ru.wb.go.mvvm.model.base.BaseItem

sealed class CourierOrdersState {

    data class ShowOrders(val items: List<BaseItem>) : CourierOrdersState()

    data class Empty(val info: String) : CourierOrdersState()

}
