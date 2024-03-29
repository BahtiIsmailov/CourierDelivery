package ru.wb.go.ui.courierorders

import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.courierwarehouses.items.CourierOrderItem
import ru.wb.go.ui.courierwarehouses.CourierWarehousesResourceProvider

class CourierOrdersDataBuilderImpl(
    private val resourceProvider: CourierWarehousesResourceProvider,
) : CourierOrdersDataBuilder {

    override fun buildOrderItem(
        lineNumber: String,
        index: Int,
        courierOrderLocalDataEntity: CourierOrderLocalDataEntity,
        isSelected: Boolean
    ): BaseItem {
        with(courierOrderLocalDataEntity) {
            //val decim = DecimalFormat("#,###.##")
            //val coast = decim.format(courierOrderLocalEntity.minCost)
            return CourierOrderItem(
                lineNumber = lineNumber,
                orderId = resourceProvider.getOrder(courierOrderLocalEntity.id),
                cost = resourceProvider.getCost(courierOrderLocalEntity.minCost),
                cargo = resourceProvider.getCargo(
                    courierOrderLocalEntity.minVolume,
                    courierOrderLocalEntity.minBoxesCount
                 ),
                countPvz = resourceProvider.getCountPvz(dstOffices.size),
                arrive = resourceProvider.getArrive(courierOrderLocalEntity.reservedDuration),
                idView = index,
                isSelected = isSelected,
                taskDistance = courierOrderLocalEntity.taskDistance

            )
        }
    }
}