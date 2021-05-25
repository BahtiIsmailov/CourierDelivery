package com.wb.logistics.ui.dcforcedtermination

import com.wb.logistics.db.entity.dcunloadedboxes.DcNotUnloadedBoxEntity

interface DcForcedTerminationDetailsDataBuilder {
    fun buildDcForcedTerminationItem(value: IndexedValue<DcNotUnloadedBoxEntity>): DcForcedTerminationDetailsItem
}