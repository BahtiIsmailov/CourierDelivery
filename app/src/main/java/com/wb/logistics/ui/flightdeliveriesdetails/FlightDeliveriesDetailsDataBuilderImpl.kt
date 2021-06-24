package com.wb.logistics.ui.flightdeliveriesdetails

import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.mvvm.model.base.BaseItem
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
        unloadedBoxes: List<FlightBoxEntity>,
        idx: Int,
    ): Int {
        var idx1 = idx
        items.add(FlightDeliveriesDetailsTitleItem(
            resourceProvider.getDeliveryTitle(),
            resourceProvider.getCountTitle(unloadedBoxes.size),
            false,
            ++idx1))
        if (unloadedBoxes.isNotEmpty()) {
            for ((index, unloadedBox) in unloadedBoxes.withIndex()) {
                items.add(FlightDeliveriesDetailsItem(upIndex(index),
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
                items.add(FlightDeliveriesDetailsItem(upIndex(index),
                    returnBox.barcode,
                    getReturnDate(returnBox.updatedAt, returnBox.updatedAt),
                    ++idx1))
            }
        }
    }

    private fun upIndex(index: Int) = (index + 1).toString()

    private fun getDeliveryDate(onlyDate: String): String {
        return resourceProvider.getDeliveryDate(getOnlyDate(onlyDate), getOnlyTime(onlyDate))
    }

    private fun getReturnDate(onlyDate: String, address: String): String {
        return resourceProvider.getReturnDate(getOnlyDate(onlyDate), getOnlyTime(onlyDate), address)
    }

    private fun getOnlyDate(date: String): String {
        return timeFormatter.format(date, ONLY_DATE)
    }

    private fun getOnlyTime(date: String): String {
        return timeFormatter.format(date, ONLY_TIME)
    }

}