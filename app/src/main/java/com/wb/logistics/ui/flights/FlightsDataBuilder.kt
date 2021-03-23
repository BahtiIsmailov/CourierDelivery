package com.wb.logistics.ui.flights

import com.wb.logistics.mvvm.model.base.BaseItem

interface FlightsDataBuilder {
    fun buildFlights(): List<BaseItem?>
}