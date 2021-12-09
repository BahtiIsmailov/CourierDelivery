package ru.wb.go.ui.flightdeliveriesdetails

import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.flightdeliveriesdetails.domain.UnloadedAndReturnBoxesGroupByOffice

interface FlightDeliveriesDetailsDataBuilder {
    fun buildItem(value: UnloadedAndReturnBoxesGroupByOffice): List<BaseItem>
}