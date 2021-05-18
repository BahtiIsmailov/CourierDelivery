package com.wb.logistics.ui.flightdeliveriesdetails

import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flightdeliveriesdetails.domain.UnloadedAndReturnBoxesGroupByOffice

interface FlightDeliveriesDetailsDataBuilder {
    fun buildItem(value: UnloadedAndReturnBoxesGroupByOffice): List<BaseItem>
}