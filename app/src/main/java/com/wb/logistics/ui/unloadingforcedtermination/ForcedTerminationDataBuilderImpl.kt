package com.wb.logistics.ui.unloadingforcedtermination

import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.utils.time.TimeFormatType.ONLY_DATE
import com.wb.logistics.utils.time.TimeFormatType.ONLY_TIME
import com.wb.logistics.utils.time.TimeFormatter

class ForcedTerminationDataBuilderImpl(
    private val timeFormatter: TimeFormatter,
    private val resourceProvider: ForcedTerminationResourceProvider,
) : ForcedTerminationDataBuilder {

    override fun buildForcedTerminationItem(value: IndexedValue<FlightBoxEntity>): ForcedTerminationItem {
        val item = value.value
        val dateFormat = timeFormatter.dateTimeWithoutTimezoneFromString(item.updatedAt)
        val data = resourceProvider.getNotDeliveryDate(
            timeFormatter.format(dateFormat, ONLY_DATE),
            timeFormatter.format(dateFormat, ONLY_TIME))
        val indexUnnamedBarcode =
            resourceProvider.getIndexUnnamedBarcode(value.index + 1, item.barcode)
        return ForcedTerminationItem(indexUnnamedBarcode, data)
    }

}