package ru.wb.go.ui.courierorders

import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.mvvm.model.base.BaseItem

interface CourierOrdersDataBuilder {
    fun buildOrderItem(
        lineNumber: String,
        index: Int,
        courierOrderLocalDataEntity: CourierOrderLocalDataEntity,
        isSelected: Boolean
    ): BaseItem
}