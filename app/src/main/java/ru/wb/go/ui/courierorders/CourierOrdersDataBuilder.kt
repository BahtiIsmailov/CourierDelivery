package ru.wb.go.ui.courierorders

import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.mvvm.model.base.BaseItem

interface CourierOrdersDataBuilder {
    fun buildOrderItem(id: String, index: Int, courierOrderEntity: CourierOrderEntity, isSelected: Boolean): BaseItem
}