package com.wb.logistics.ui.flightdeliveries

import com.wb.logistics.db.entity.deliveryboxes.DeliveryBoxGroupByOfficeEntity
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flightdeliveries.delegates.items.*

class FlightDeliveriesDataBuilderImpl(
    private val resourceProvider: FlightDeliveriesResourceProvider,
) : FlightDeliveriesDataBuilder {

    override fun buildPvzSuccessItem(
        index: Int,
        deliveryBoxGroupByOfficeEntity: DeliveryBoxGroupByOfficeEntity,
    ): BaseItem {
        with(deliveryBoxGroupByOfficeEntity) {
            if (returnedCount > 0 || deliveredCount > 0) {
                val returnCountText =
                    if (returnedCount > 0) resourceProvider.getReturnedCount(returnedCount)
                    else resourceProvider.getEmptyCount()
                return if (deliverCount > 0) {
                    FlightDeliveriesNotUnloadItem(
                        address = dstFullAddress,
                        unloadedCount = resourceProvider.getNotDelivery(deliveredCount,
                            deliveredCount + deliverCount),
                        returnCount = returnCountText,
                        idView = index)

                } else {
                    FlightDeliveriesUnloadItem(
                        address = dstFullAddress,
                        unloadedCount = resourceProvider.getDelivery(deliveredCount),
                        returnCount = returnCountText,
                        idView = index)
                }

            } else {
                return FlightDeliveriesItem(
                    address = dstFullAddress,
                    deliverCount = resourceProvider.getDeliverCount(deliverCount),
                    returnedCount =
                    if (returnCount > 0) resourceProvider.getReturnCount(returnCount)
                    else resourceProvider.getEmptyCount(),
                    isEnabled = true,
                    idView = index
                )
            }
        }
    }

    override fun buildEmptyItem(): BaseItem =
        FlightDeliveriesRefreshItem("Список пуст")

    override fun buildProgressItem(): BaseItem = FlightDeliveriesProgressItem()

    override fun buildErrorItem(): BaseItem =
        FlightDeliveriesRefreshItem("Ошибка получения данных")

    override fun buildErrorMessageItem(message: String): BaseItem =
        FlightDeliveriesRefreshItem(message)

}