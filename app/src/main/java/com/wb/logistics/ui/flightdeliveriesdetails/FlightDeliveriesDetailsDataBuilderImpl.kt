package com.wb.logistics.ui.flightdeliveriesdetails

import com.wb.logistics.db.entity.deliveryerrorbox.DeliveryErrorBoxEntity
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
        idx = buildUnloadedItems(items, value.errorBoxes, value.unloadedBoxes, idx)
        buildReturnItems(items, value.returnBoxes, idx)
        return items
    }

    private fun buildUnloadedItems(
        items: MutableList<BaseItem>,
        errorBoxes: List<DeliveryErrorBoxEntity>,
        unloadedBoxes: List<FlightBoxEntity>,
        idx: Int,
    ): Int {
        var idx1 = idx
        items.add(FlightDeliveriesDetailsTitleItem(
            resourceProvider.getDeliveryTitle(),
            resourceProvider.getCountTitle(errorBoxes.size + unloadedBoxes.size),
            false,
            ++idx1))

        if (errorBoxes.isNotEmpty()) {
            for ((index, errorBox) in errorBoxes.withIndex()) {
                items.add(FlightDeliveriesDetailsErrorItem(
                    errorBox.barcode,
                    getDeliveryError(errorBox.updatedAt, errorBox.fullAddress),
                    ++idx1))
            }
        }
        if (unloadedBoxes.isNotEmpty()) {
            for ((index, unloadedBox) in unloadedBoxes.withIndex()) {
                items.add(FlightDeliveriesDetailsItem(
                    unloadedBox.barcode,
                    getDeliveryDate(unloadedBox.updatedAt),
                    ++idx1))
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
                    returnBox.barcode,
                    getReturnDate(returnBox.updatedAt, returnBox.srcOffice.fullAddress),
                    ++idx1))
            }
        }
    }

    private fun getDeliveryDate(onlyDate: String): String {
        return resourceProvider.getDeliveryDate(getOnlyDate(onlyDate), getOnlyTime(onlyDate))
    }

    private fun getDeliveryError(onlyDate: String, address: String): String {
        return if (address.isEmpty())
            resourceProvider.getInfoEmptyPvzError(getOnlyDate(onlyDate), getOnlyTime(onlyDate))
        else
            resourceProvider.getNotBelongPvzError(getOnlyDate(onlyDate),
                getOnlyTime(onlyDate),
                address)
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