package ru.wb.go.ui.dcunloadingforcedtermination

import ru.wb.go.db.entity.dcunloadedboxes.DcNotUnloadedBoxEntity

interface DcForcedTerminationDetailsDataBuilder {
    fun buildDcForcedTerminationItem(value: IndexedValue<DcNotUnloadedBoxEntity>): DcForcedTerminationDetailsItem
}