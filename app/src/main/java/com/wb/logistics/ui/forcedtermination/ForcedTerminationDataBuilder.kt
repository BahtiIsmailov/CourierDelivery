package com.wb.logistics.ui.forcedtermination

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity

interface ForcedTerminationDataBuilder {
    fun buildForcedTerminationItem(value: IndexedValue<AttachedBoxEntity>): ForcedTerminationItem
}