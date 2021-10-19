package ru.wb.perevozka.ui.courierorders

import ru.wb.perevozka.db.entity.courier.CourierOrderEntity
import ru.wb.perevozka.mvvm.model.base.BaseItem
import ru.wb.perevozka.ui.courierorders.delegates.items.CourierOrderItem
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
            coast = "" + coast + " ₽",
            idView = index
        )
    }

}