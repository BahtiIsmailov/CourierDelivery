package ru.wb.perevozka.ui.unloadingforcedtermination

import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity

interface ForcedTerminationDataBuilder {
    fun buildForcedTerminationItem(value: IndexedValue<FlightBoxEntity>): ForcedTerminationItem
}