package com.wb.logistics.ui.flights

import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flights.domain.FlightEntity
import com.wb.logistics.ui.flights.domain.FlightsData

interface FlightsDataBuilder {
    fun buildSuccessItem(flightEntity: FlightEntity.Success<FlightsData>): BaseItem
    fun buildEmptyItem(): BaseItem
    fun buildProgressItem(): BaseItem
    fun buildErrorItem(): BaseItem
    fun buildErrorMessageItem(message : String): BaseItem
}