package ru.wb.go.ui.flights

import ru.wb.go.db.FlightData
import ru.wb.go.db.Optional
import ru.wb.go.mvvm.model.base.BaseItem

interface FlightsDataBuilder {
    fun buildSuccessItem(flightEntity: Optional.Success<FlightData>): BaseItem
    fun buildEmptyItem(): BaseItem
    fun buildProgressItem(): BaseItem
    fun buildErrorItem(): BaseItem
    fun buildErrorMessageItem(message : String): BaseItem
}