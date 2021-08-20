package ru.wb.perevozka.ui.courierorders

import ru.wb.perevozka.db.entity.courier.CourierOrderEntity
import ru.wb.perevozka.mvvm.model.base.BaseItem

interface CourierOrderDataBuilder {
    fun buildOrderItem(courierOrderEntity: CourierOrderEntity): BaseItem
}