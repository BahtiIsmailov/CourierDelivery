package ru.wb.go.ui.courierorders

import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.courierorders.delegates.items.CourierOrderItem
import java.text.DecimalFormat

class CourierOrdersDataBuilderImpl(
    private val resourceProvider: CourierOrdersResourceProvider,
) : CourierOrdersDataBuilder {

    override fun buildOrderItem(index: Int, courierOrderEntity: CourierOrderEntity): BaseItem {
        val decim = DecimalFormat("#,###.##")
        val coast = decim.format(courierOrderEntity.minPrice)
        return CourierOrderItem(
            order = "Заказ " + courierOrderEntity.id.toString(),
            arrive = resourceProvider.getArrive(courierOrderEntity.reservedDuration),
            volume = resourceProvider.getBoxCountAndVolume(
                courierOrderEntity.minBoxesCount,
                courierOrderEntity.minVolume
            ),
            pvzCount = "" + courierOrderEntity.dstOffices.size + " ПВЗ",
            coast = "от " + coast + " ₽",
            idView = index
        )
    }

}