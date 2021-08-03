package com.wb.logistics.ui.flightdeliveriesdetails

import com.wb.logistics.db.entity.deliveryerrorbox.DeliveryUnloadingErrorBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flightdeliveriesdetails.delegates.items.FlightDeliveriesDetailsErrorItem
import com.wb.logistics.ui.flightdeliveriesdetails.delegates.items.FlightDeliveriesDetailsItem
import com.wb.logistics.ui.flightdeliveriesdetails.delegates.items.FlightDeliveriesDetailsTitleItem
import com.wb.logistics.ui.flightdeliveriesdetails.domain.UnloadedAndReturnBoxesGroupByOffice
import com.wb.logistics.utils.time.TimeFormatType.ONLY_DATE
import com.wb.logistics.utils.time.TimeFormatType.ONLY_TIME
import com.wb.logistics.utils.time.TimeFormatter

class FlightDeliveriesDetailsDataBuilderImpl(
    private val timeFormatter: TimeFormatter,
    private val resourceProvider: FlightDeliveriesDetailsResourceProvider,
) : FlightDeliveriesDetailsDataBuilder {

    override fun buildItem(value: UnloadedAndReturnBoxesGroupByOffice): List<BaseItem> {
        val items = mutableListOf<BaseItem>()
        var idx = 0

        idx = buildUnloadedItems(items, value.unloadedBoxes, idx)
        buildReturnItems(items, value.returnBoxes, idx)
        return items
    }

    private fun buildUnloadedItems(
        items: MutableList<BaseItem>,
        unloadedBoxes: List<DeliveryUnloadingErrorBoxEntity>,
        idx: Int,
    ): Int {
        var idx1 = idx
        items.add(FlightDeliveriesDetailsTitleItem(
            resourceProvider.getDeliveryTitle(),
            resourceProvider.getCountTitle(unloadedBoxes.size),
            false,
            ++idx1))

        if (unloadedBoxes.isNotEmpty()) {
            for (unloadedBox in unloadedBoxes) {
                with(unloadedBox) {
                    val errorOfficeId = errorOfficeId
                    val item = if (errorOfficeId == null) {
                        if (onBoard) {
                            FlightDeliveriesDetailsErrorItem(
                                resourceProvider.getUnnamedBarcodeFormat(barcode),
                                resourceProvider.getNotFoundOnUnloading(getOnlyDate(updatedAt),
                                    getOnlyTime(updatedAt)),
                                ++idx1)

                        } else {
                            FlightDeliveriesDetailsItem(
                                resourceProvider.getUnnamedBarcodeFormat(barcode),
                                getDeliveryDate(updatedAt),
                                ++idx1)
                        }
                    } else {
                        if (unloadedBox.dstOfficeId == errorOfficeId) {
                            FlightDeliveriesDetailsErrorItem(
                                resourceProvider.getUnnamedBarcodeFormat(barcode),
                                resourceProvider.getNotFoundOnUnloading(getOnlyDate(updatedAt),
                                    getOnlyTime(updatedAt)),
                                ++idx1)

                        } else {
                            FlightDeliveriesDetailsErrorItem(
                                resourceProvider.getUnnamedBarcodeFormat(barcode),
                                resourceProvider.getTriedToUnload(getOnlyDate(updatedAt),
                                    getOnlyTime(updatedAt),
                                    errorOfficeFullAddress ?: ""),
                                ++idx1)
                        }
                    }
                    items.add(item)
                }
            }
        }
        return idx1
    }

    private fun buildReturnItems(
        items: MutableList<BaseItem>,
        returnBoxes: List<FlightBoxEntity>,
        idx: Int,
    ) {
        var idx1 = idx
        items.add(FlightDeliveriesDetailsTitleItem(
            resourceProvider.getReturnTitle(),
            resourceProvider.getCountTitle(returnBoxes.size),
            true,
            ++idx1))
        if (returnBoxes.isNotEmpty()) {
            for ((index, returnBox) in returnBoxes.withIndex()) {
                items.add(FlightDeliveriesDetailsItem(
                    resourceProvider.getUnnamedBarcodeFormat(returnBox.barcode),
                    getReturnDate(returnBox.updatedAt, returnBox.srcOffice.fullAddress),
                    ++idx1))
            }
        }
    }

    private fun getDeliveryDate(onlyDate: String): String {
        return resourceProvider.getDeliveryDate(getOnlyDate(onlyDate), getOnlyTime(onlyDate))
    }

    private fun getReturnDate(onlyDate: String, address: String): String {
        return resourceProvider.getReturnDate(getOnlyDate(onlyDate), getOnlyTime(onlyDate), address)
    }

    private fun getOnlyDate(date: String): String {
        val convertDate = timeFormatter.dateTimeWithoutTimezoneFromString(date)
        return timeFormatter.format(convertDate, ONLY_DATE)
    }

    private fun getOnlyTime(date: String): String {
        val convertDate = timeFormatter.dateTimeWithoutTimezoneFromString(date)
        return timeFormatter.format(convertDate, ONLY_TIME)
    }

}