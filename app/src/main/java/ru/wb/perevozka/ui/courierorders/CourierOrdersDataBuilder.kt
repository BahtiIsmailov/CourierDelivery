package ru.wb.perevozka.ui.courierorders

import ru.wb.perevozka.db.entity.courier.CourierOrderEntity
import ru.wb.perevozka.mvvm.model.base.BaseItem

interface CourierOrdersDataBuilder {
    fun buildOrderItem(index: Int, courierOrderEntity: CourierOrderEntity): BaseItem
}