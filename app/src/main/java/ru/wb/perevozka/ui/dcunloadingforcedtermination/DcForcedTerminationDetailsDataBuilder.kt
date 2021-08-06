package ru.wb.perevozka.ui.dcunloadingforcedtermination

import ru.wb.perevozka.db.entity.dcunloadedboxes.DcNotUnloadedBoxEntity

interface DcForcedTerminationDetailsDataBuilder {
    fun buildDcForcedTerminationItem(value: IndexedValue<DcNotUnloadedBoxEntity>): DcForcedTerminationDetailsItem
}