package ru.wb.perevozka.ui.courierorders

import ru.wb.perevozka.mvvm.model.base.BaseItem

sealed class CourierOrdersState {

    data class ShowOrders(val items: List<BaseItem>) : CourierOrdersState()

    data class Empty(val info: String) : CourierOrdersState()

}
