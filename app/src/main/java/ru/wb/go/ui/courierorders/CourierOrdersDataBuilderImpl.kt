package ru.wb.go.ui.courierorders

import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.courierorders.delegates.items.CourierOrderItem
import java.text.DecimalFormat

class CourierOrdersDataBuilderImpl(
    private val resourceProvider: CourierOrdersResourceProvider,
) : CourierOrdersDataBuilder {

    override fun buildOrderItem(
        id: String,
        index: Int,
        courierOrderEntity: CourierOrderEntity,
        isSelected: Boolean
    ): BaseItem {
        val decim = DecimalFormat("#,###.##")
        val coast = decim.format(courierOrderEntity.minPrice)
        return CourierOrderItem(
            orderId = id,
            order = resourceProvider.getOrder(courierOrderEntity.id),
            cost = resourceProvider.getCoast(coast),
            countBox = resourceProvider.getBoxCountBox(courierOrderEntity.minBoxesCount),
            volume = resourceProvider.getVolume(courierOrderEntity.minVolume),
            countPvz = resourceProvider.getCountPvz(courierOrderEntity.dstOffices.size),
            arrive = resourceProvider.getArrive(courierOrderEntity.reservedDuration),
            isSelected = isSelected,
            idView = index
        )
    }

}