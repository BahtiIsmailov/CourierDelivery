package com.wb.logistics.ui.flights

import com.wb.logistics.db.FlightData
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.mvvm.model.base.BaseItem

interface FlightsDataBuilder {
    fun buildSuccessItem(flightEntity: SuccessOrEmptyData.Success<FlightData>): BaseItem
    fun buildEmptyItem(): BaseItem
    fun buildProgressItem(): BaseItem
    fun buildErrorItem(): BaseItem
    fun buildErrorMessageItem(message : String): BaseItem
}