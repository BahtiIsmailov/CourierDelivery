package ru.wb.perevozka.ui.flights

import ru.wb.perevozka.db.FlightData
import ru.wb.perevozka.db.Optional
import ru.wb.perevozka.mvvm.model.base.BaseItem

interface FlightsDataBuilder {
    fun buildSuccessItem(flightEntity: Optional.Success<FlightData>): BaseItem
    fun buildEmptyItem(): BaseItem
    fun buildProgressItem(): BaseItem
    fun buildErrorItem(): BaseItem
    fun buildErrorMessageItem(message : String): BaseItem
}