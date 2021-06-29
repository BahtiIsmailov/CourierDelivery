package com.wb.logistics.ui.dcunloadingforcedtermination

import com.wb.logistics.db.entity.dcunloadedboxes.DcNotUnloadedBoxEntity

interface DcForcedTerminationDetailsDataBuilder {
    fun buildDcForcedTerminationItem(value: IndexedValue<DcNotUnloadedBoxEntity>): DcForcedTerminationDetailsItem
}