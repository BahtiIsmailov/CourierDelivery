package com.wb.logistics.ui.flights

import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flights.res.DeliveryResourceProvider

class FlightsDataBuilderImpl(private val resourceProvider: DeliveryResourceProvider) :
    FlightsDataBuilder {
    override fun buildFlights(): List<BaseItem> {
        return ArrayList()
    }
}