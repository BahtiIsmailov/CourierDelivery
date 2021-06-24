package com.wb.logistics.ui.unloadingforcedtermination

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity

interface ForcedTerminationDataBuilder {
    fun buildForcedTerminationItem(value: IndexedValue<AttachedBoxEntity>): ForcedTerminationItem
}