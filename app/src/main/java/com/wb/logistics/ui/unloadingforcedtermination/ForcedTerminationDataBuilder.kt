package com.wb.logistics.ui.unloadingforcedtermination

import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity

interface ForcedTerminationDataBuilder {
    fun buildForcedTerminationItem(value: IndexedValue<FlightBoxEntity>): ForcedTerminationItem
}