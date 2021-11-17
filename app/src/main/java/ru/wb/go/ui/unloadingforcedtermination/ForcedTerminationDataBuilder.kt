package ru.wb.go.ui.unloadingforcedtermination

import ru.wb.go.db.entity.flighboxes.FlightBoxEntity

interface ForcedTerminationDataBuilder {
    fun buildForcedTerminationItem(value: IndexedValue<FlightBoxEntity>): ForcedTerminationItem
}