package com.wb.logistics.ui.dcforcedtermination

import com.wb.logistics.db.entity.dcunloadedboxes.DcNotUnloadedBoxEntity
import com.wb.logistics.utils.time.TimeFormatType.ONLY_DATE
import com.wb.logistics.utils.time.TimeFormatType.ONLY_TIME
import com.wb.logistics.utils.time.TimeFormatter

class DcForcedTerminationDetailsDataBuilderImpl(
    private val timeFormatter: TimeFormatter,
    private val resourceProvider: DcForcedTerminationDetailsResourceProvider,
) : DcForcedTerminationDetailsDataBuilder {

    override fun buildDcForcedTerminationItem(value: IndexedValue<DcNotUnloadedBoxEntity>): DcForcedTerminationDetailsItem {
        val item = value.value
        val date = item.updatedAt
        val data = if (item.dstFullAddress.isEmpty()) {
            resourceProvider.getNotDelivery(
                timeFormatter.format(date, ONLY_DATE),
                timeFormatter.format(date, ONLY_TIME))
        } else {
            resourceProvider.getNotReturned(
                timeFormatter.format(date, ONLY_DATE),
                timeFormatter.format(date, ONLY_TIME),
                item.dstFullAddress)
        }
        return DcForcedTerminationDetailsItem((value.index + 1).toString(), item.barcode, data)
    }

}