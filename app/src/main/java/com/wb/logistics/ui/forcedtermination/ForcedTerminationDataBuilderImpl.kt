package com.wb.logistics.ui.forcedtermination

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.utils.time.TimeFormatType.ONLY_DATE
import com.wb.logistics.utils.time.TimeFormatType.ONLY_TIME
import com.wb.logistics.utils.time.TimeFormatter

class ForcedTerminationDataBuilderImpl(
    private val timeFormatter: TimeFormatter,
    private val resourceProvider: ForcedTerminationResourceProvider,
) : ForcedTerminationDataBuilder {

    override fun buildForcedTerminationItem(value: IndexedValue<AttachedBoxEntity>): ForcedTerminationItem {
        val item = value.value
        val date = item.updatedAt
        val data = resourceProvider.getNotDeliveryDate(
            timeFormatter.format(date, ONLY_DATE),
            timeFormatter.format(date, ONLY_TIME))
        return ForcedTerminationItem((value.index + 1).toString(), item.barcode, data)
    }

}